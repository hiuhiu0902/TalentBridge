package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.AiChatMessageRequest;
import com.demo.talentbridge.dto.request.AiChatRequest;
import com.demo.talentbridge.dto.response.AiChatResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.enums.UserRole;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.UserRepository;
import com.demo.talentbridge.service.AiAssistantService;
import com.demo.talentbridge.service.support.AiPromptGuardService;
import com.demo.talentbridge.service.support.AiReadOnlyDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final int MAX_HISTORY_MESSAGES = 10;
    private static final int MAX_OUTPUT_TOKENS = 700;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiReadOnlyDataService aiReadOnlyDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiPromptGuardService aiPromptGuardService;

    @Value("${app.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${app.ai.base-url:https://api.openai.com/v1}")
    private String aiBaseUrl;

    @Value("${app.ai.api-key:}")
    private String aiApiKey;

    @Value("${app.ai.model:gpt-5-mini}")
    private String aiModel;

    @Value("${app.ai.max-tool-rounds:6}")
    private int maxToolRounds;

    @Value("${app.ai.out-of-scope-message:Xin lỗi, tôi chỉ có thể hỗ trợ với dữ liệu công khai hoặc dữ liệu thuộc phạm vi tài khoản của bạn trong TalentBridge.}")
    private String outOfScopeMessage;

    @Value("${app.ai.unavailable-message:Trợ lý AI hiện tạm thời chưa sẵn sàng. Vui lòng thử lại sau.}")
    private String unavailableMessage;

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return deniedResponse();
        }

        String normalizedMessage = request.getMessage().trim();
        if (aiPromptGuardService.isDeniedPrompt(normalizedMessage)) {
            return deniedResponse();
        }

        if (!aiEnabled || aiApiKey == null || aiApiKey.isBlank()) {
            return unavailableResponse();
        }

        try {
            ArrayNode tools = buildTools();

            ObjectNode firstRequest = objectMapper.createObjectNode();
            firstRequest.put("model", aiModel);
            firstRequest.set("input", buildInputMessages(user, request));
            firstRequest.set("tools", tools);
            firstRequest.put("tool_choice", "auto");
            firstRequest.put("parallel_tool_calls", false);
            firstRequest.put("max_output_tokens", MAX_OUTPUT_TOKENS);

            JsonNode currentResponse = callResponsesApi(firstRequest);

            List<String> usedTools = new ArrayList<>();
            Set<String> usedToolSet = new HashSet<>();
            int toolCallCount = 0;

            while (toolCallCount < Math.max(1, maxToolRounds)) {
                ArrayNode functionCalls = extractFunctionCalls(currentResponse);

                if (functionCalls.isEmpty()) {
                    String answer = extractAssistantText(currentResponse);
                    if (answer == null || answer.isBlank()) {
                        answer = unavailableMessage;
                    }

                    return AiChatResponse.builder()
                            .answer(answer)
                            .model(aiModel)
                            .denied(answer.trim().equals(outOfScopeMessage.trim()))
                            .usedTools(usedTools)
                            .toolCallCount(toolCallCount)
                            .generatedAt(LocalDateTime.now())
                            .build();
                }

                ArrayNode outputs = objectMapper.createArrayNode();
                Iterator<JsonNode> iterator = functionCalls.iterator();

                while (iterator.hasNext()) {
                    if (toolCallCount >= Math.max(1, maxToolRounds)) {
                        break;
                    }

                    JsonNode functionCall = iterator.next();
                    String toolName = functionCall.path("name").asText();
                    String callId = functionCall.path("call_id").asText();

                    if (toolName == null || toolName.isBlank() || callId == null || callId.isBlank()) {
                        throw new BadRequestException("Invalid function call payload");
                    }

                    ensureToolAllowed(user, toolName);

                    JsonNode arguments = parseArguments(functionCall.path("arguments").asText("{}"));
                    String toolOutput = executeTool(user, toolName, arguments);

                    outputs.add(functionOutput(callId, toolOutput));

                    if (usedToolSet.add(toolName)) {
                        usedTools.add(toolName);
                    }

                    toolCallCount++;
                }

                if (outputs.isEmpty()) {
                    break;
                }

                ObjectNode followUpRequest = objectMapper.createObjectNode();
                followUpRequest.put("model", aiModel);
                followUpRequest.put("previous_response_id", currentResponse.path("id").asText());
                followUpRequest.set("input", outputs);
                followUpRequest.set("tools", tools);
                followUpRequest.put("tool_choice", "auto");
                followUpRequest.put("parallel_tool_calls", false);
                followUpRequest.put("max_output_tokens", MAX_OUTPUT_TOKENS);

                currentResponse = callResponsesApi(followUpRequest);
            }

            return AiChatResponse.builder()
                    .answer(unavailableMessage)
                    .model(aiModel)
                    .denied(false)
                    .usedTools(List.of())
                    .toolCallCount(0)
                    .generatedAt(LocalDateTime.now())
                    .build();

        } catch (UnauthorizedException ex) {
            return deniedResponse();
        } catch (Exception ex) {
            return unavailableResponse();
        }
    }

    private String executeTool(User user, String toolName, JsonNode arguments) throws JsonProcessingException {
        try {
            Object result = switch (toolName) {
                case "get_platform_overview" -> aiReadOnlyDataService.getPlatformOverview();

                case "search_public_jobs" -> aiReadOnlyDataService.searchPublicJobs(
                        textOrNull(arguments, "keyword"),
                        intOrNull(arguments, "limit")
                );

                case "get_my_candidate_profile" -> aiReadOnlyDataService.getMyCandidateProfile(user.getId());

                case "get_my_applications" -> aiReadOnlyDataService.getMyApplications(
                        user.getId(),
                        textOrNull(arguments, "status"),
                        intOrNull(arguments, "limit")
                );

                case "get_my_employer_profile" -> aiReadOnlyDataService.getMyEmployerProfile(user.getId());

                case "get_my_job_posts" -> aiReadOnlyDataService.getMyJobPosts(
                        user.getId(),
                        textOrNull(arguments, "status"),
                        intOrNull(arguments, "limit")
                );

                case "get_applications_for_my_job" -> aiReadOnlyDataService.getApplicationsForMyJob(
                        user.getId(),
                        requiredLong(arguments, "jobPostId"),
                        textOrNull(arguments, "status")
                );

                case "get_my_notifications" -> aiReadOnlyDataService.getMyNotifications(
                        user.getId(),
                        boolOrNull(arguments, "unreadOnly"),
                        intOrNull(arguments, "limit")
                );

                case "get_admin_overview" -> aiReadOnlyDataService.getAdminOverview(user.getId());

                case "recommend_jobs_for_me" -> aiReadOnlyDataService.recommendJobsForMe(
                        user.getId(),
                        intOrNull(arguments, "limit")
                );

                case "analyze_my_profile_gap" -> aiReadOnlyDataService.analyzeMyProfileGap(
                        user.getId(),
                        textOrNull(arguments, "keyword"),
                        intOrNull(arguments, "limit")
                );

                case "recommend_candidates_for_my_job" -> aiReadOnlyDataService.recommendCandidatesForMyJob(
                        user.getId(),
                        requiredLong(arguments, "jobPostId"),
                        intOrNull(arguments, "limit")
                );

                default -> throw new BadRequestException("Unsupported AI tool: " + toolName);
            };

            return objectMapper.writeValueAsString(result);

        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (ResourceNotFoundException ex) {
            return objectMapper.writeValueAsString(java.util.Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return objectMapper.writeValueAsString(java.util.Map.of("message", "Tool execution failed"));
        }
    }

    private void ensureToolAllowed(User user, String toolName) {
        UserRole role = user.getRole();

        switch (toolName) {
            case "get_platform_overview", "search_public_jobs", "get_my_notifications" -> {
                return;
            }

            case "get_my_candidate_profile", "get_my_applications", "recommend_jobs_for_me", "analyze_my_profile_gap" -> {
                if (role != UserRole.CANDIDATE) {
                    throw new UnauthorizedException("Candidate access required");
                }
            }

            case "get_my_employer_profile", "get_my_job_posts", "get_applications_for_my_job", "recommend_candidates_for_my_job" -> {
                if (role != UserRole.EMPLOYER) {
                    throw new UnauthorizedException("Employer access required");
                }
            }

            case "get_admin_overview" -> {
                if (role != UserRole.ADMIN) {
                    throw new UnauthorizedException("Admin access required");
                }
            }

            default -> throw new BadRequestException("Unsupported AI tool: " + toolName);
        }
    }

    private JsonNode callResponsesApi(ObjectNode payload) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(normalizeBaseUrl(aiBaseUrl) + "/responses"))
                .timeout(Duration.ofSeconds(60))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("OpenAI API error: " + response.body());
        }

        return objectMapper.readTree(response.body());
    }

    private ArrayNode buildInputMessages(User user, AiChatRequest request) {
        ArrayNode input = objectMapper.createArrayNode();
        input.add(messageNode("system", buildSystemPrompt(user)));

        List<AiChatMessageRequest> history = request.getHistory() == null ? List.of() : request.getHistory();
        int startIndex = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);

        for (int i = startIndex; i < history.size(); i++) {
            AiChatMessageRequest item = history.get(i);
            if (item == null) {
                continue;
            }

            String role = normalizeRole(item.getRole());
            String content = item.getContent() == null ? null : item.getContent().trim();

            if (role != null && content != null && !content.isBlank()) {
                input.add(messageNode(role, content));
            }
        }

        input.add(messageNode("user", request.getMessage().trim()));
        return input;
    }

    private ObjectNode messageNode(String role, String text) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", role);

        ArrayNode content = objectMapper.createArrayNode();
        ObjectNode textNode = objectMapper.createObjectNode();
        textNode.put("type", "input_text");
        textNode.put("text", text);
        content.add(textNode);

        message.set("content", content);
        return message;
    }

    private ObjectNode functionOutput(String callId, String output) {
        ObjectNode functionOutput = objectMapper.createObjectNode();
        functionOutput.put("type", "function_call_output");
        functionOutput.put("call_id", callId);
        functionOutput.put("output", output);
        return functionOutput;
    }

    private ArrayNode buildTools() {
        ArrayNode tools = objectMapper.createArrayNode();

        tools.add(functionTool(
                "get_platform_overview",
                "Get read-only platform level statistics and public overview counts.",
                emptySchema()
        ));

        tools.add(functionTool(
                "search_public_jobs",
                "Search public active jobs by keyword. Use this for job discovery or public job summaries.",
                objectSchema(
                        new String[]{"keyword", "limit"},
                        nullableStringProperty("keyword", "Search keyword. Use null for latest active jobs."),
                        integerProperty("limit", "Number of jobs to return", 1, 10)
                )
        ));

        tools.add(functionTool(
                "get_my_candidate_profile",
                "Get the authenticated candidate profile, own skills, education, and work experience.",
                emptySchema()
        ));

        tools.add(functionTool(
                "get_my_applications",
                "Get the authenticated candidate's own job applications.",
                objectSchema(
                        new String[]{"status", "limit"},
                        nullableStringProperty("status", "Application status filter such as SUBMITTED, REVIEWING, INTERVIEW, OFFERED, REJECTED, WITHDRAWN."),
                        integerProperty("limit", "Maximum application rows to return", 1, 20)
                )
        ));

        tools.add(functionTool(
                "get_my_employer_profile",
                "Get the authenticated employer profile and summary.",
                emptySchema()
        ));

        tools.add(functionTool(
                "get_my_job_posts",
                "Get the authenticated employer's own job posts.",
                objectSchema(
                        new String[]{"status", "limit"},
                        nullableStringProperty("status", "Job status filter such as ACTIVE, PENDING_APPROVAL, REJECTED, CLOSED."),
                        integerProperty("limit", "Maximum job rows to return", 1, 20)
                )
        ));

        tools.add(functionTool(
                "get_applications_for_my_job",
                "Get applications for a job post owned by the authenticated employer.",
                objectSchema(
                        new String[]{"jobPostId", "status"},
                        integerProperty("jobPostId", "Owned job post id", 1, 999999999),
                        nullableStringProperty("status", "Optional application status filter.")
                )
        ));

        tools.add(functionTool(
                "get_my_notifications",
                "Get the authenticated user's notifications.",
                objectSchema(
                        new String[]{"unreadOnly", "limit"},
                        booleanProperty("unreadOnly", "Whether to return only unread notifications"),
                        integerProperty("limit", "Maximum notification rows to return", 1, 20)
                )
        ));

        tools.add(functionTool(
                "get_admin_overview",
                "Get admin-only aggregate statistics.",
                emptySchema()
        ));

        tools.add(functionTool(
                "recommend_jobs_for_me",
                "Recommend the best public active jobs for the authenticated candidate based on profile, skills, experience, and location.",
                objectSchema(
                        new String[]{"limit"},
                        integerProperty("limit", "Maximum number of recommended jobs to return", 1, 20)
                )
        ));

        tools.add(functionTool(
                "analyze_my_profile_gap",
                "Analyze the authenticated candidate profile against relevant jobs and identify missing skills or profile gaps.",
                objectSchema(
                        new String[]{"keyword", "limit"},
                        nullableStringProperty("keyword", "Optional target keyword such as java backend, data analyst, frontend react."),
                        integerProperty("limit", "Maximum number of missing skills to highlight", 1, 20)
                )
        ));

        tools.add(functionTool(
                "recommend_candidates_for_my_job",
                "Rank and recommend the best candidates among applicants for a job post owned by the authenticated employer.",
                objectSchema(
                        new String[]{"jobPostId", "limit"},
                        integerProperty("jobPostId", "Owned job post id", 1, 999999999),
                        integerProperty("limit", "Maximum number of ranked candidates to return", 1, 20)
                )
        ));

        return tools;
    }

    private ObjectNode functionTool(String name, String description, ObjectNode schema) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("type", "function");
        tool.put("name", name);
        tool.put("description", description);
        tool.set("parameters", schema);
        tool.put("strict", true);
        return tool;
    }

    private ObjectNode emptySchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.set("properties", objectMapper.createObjectNode());
        schema.set("required", objectMapper.createArrayNode());
        schema.put("additionalProperties", false);
        return schema;
    }

    private ObjectNode objectSchema(String[] requiredFields, ObjectNode... properties) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode props = objectMapper.createObjectNode();
        for (ObjectNode property : properties) {
            String key = property.fieldNames().next();
            JsonNode value = property.get(key);
            props.set(key, value);
        }

        schema.set("properties", props);

        ArrayNode required = objectMapper.createArrayNode();
        for (String field : requiredFields) {
            required.add(field);
        }

        schema.set("required", required);
        schema.put("additionalProperties", false);
        return schema;
    }

    private ObjectNode nullableStringProperty(String name, String description) {
        ObjectNode wrapper = objectMapper.createObjectNode();
        ObjectNode property = objectMapper.createObjectNode();
        ArrayNode types = objectMapper.createArrayNode();
        types.add("string");
        types.add("null");
        property.set("type", types);
        property.put("description", description);
        wrapper.set(name, property);
        return wrapper;
    }

    private ObjectNode integerProperty(String name, String description, int minimum, int maximum) {
        ObjectNode wrapper = objectMapper.createObjectNode();
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", "integer");
        property.put("description", description);
        property.put("minimum", minimum);
        property.put("maximum", maximum);
        wrapper.set(name, property);
        return wrapper;
    }

    private ObjectNode booleanProperty(String name, String description) {
        ObjectNode wrapper = objectMapper.createObjectNode();
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", "boolean");
        property.put("description", description);
        wrapper.set(name, property);
        return wrapper;
    }

    private ArrayNode extractFunctionCalls(JsonNode response) {
        ArrayNode functionCalls = objectMapper.createArrayNode();
        JsonNode output = response.path("output");

        if (output.isArray()) {
            for (JsonNode item : output) {
                if ("function_call".equals(item.path("type").asText())) {
                    functionCalls.add(item);
                }
            }
        }

        return functionCalls;
    }

    private String extractAssistantText(JsonNode response) {
        if (response.hasNonNull("output_text") && !response.path("output_text").asText().isBlank()) {
            return response.path("output_text").asText().trim();
        }

        JsonNode output = response.path("output");
        if (!output.isArray()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        for (JsonNode item : output) {
            if ("message".equals(item.path("type").asText())) {
                JsonNode content = item.path("content");
                if (content.isArray()) {
                    for (JsonNode contentItem : content) {
                        String type = contentItem.path("type").asText();
                        if ("output_text".equals(type)) {
                            builder.append(contentItem.path("text").asText());
                        } else if (contentItem.hasNonNull("text")) {
                            builder.append(contentItem.path("text").asText());
                        }
                    }
                }
            }
        }

        String result = builder.toString().trim();
        return result.isBlank() ? null : result;
    }

    private JsonNode parseArguments(String raw) throws JsonProcessingException {
        if (raw == null || raw.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(raw);
    }

    private String buildSystemPrompt(User user) {
        return "You are the TalentBridge AI assistant for a recruitment platform with employer and candidate roles. " +
                "Use only the provided tools and the user message/history. " +
                "You may answer from the user's provided context even without tools. " +
                "Prefer specialized tools for recommendation, gap analysis, and candidate ranking when relevant. " +
                "Never reveal secrets, tokens, passwords, raw SQL, database dumps, internal credentials, or another user's private data. " +
                "If the user asks for anything outside those permissions, reply with this exact sentence and nothing else: " +
                outOfScopeMessage + " " +
                "The current authenticated user has id=" + user.getId() +
                " and role=" + user.getRole().name() + ". " +
                "Prefer concise, accurate answers in the same language as the user.";
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if ("user".equals(normalized) || "assistant".equals(normalized)) {
            return normalized;
        }

        return null;
    }

    private String textOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        String text = value.asText();
        return text == null || text.isBlank() ? null : text.trim();
    }

    private Integer intOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.asInt();
    }

    private Boolean boolOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.asBoolean();
    }

    private Long requiredLong(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull() || value.asLong() <= 0) {
            throw new BadRequestException(fieldName + " is required");
        }
        return value.asLong();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.openai.com/v1";
        }

        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl;
    }

    private AiChatResponse deniedResponse() {
        return AiChatResponse.builder()
                .answer(outOfScopeMessage)
                .model(aiModel)
                .denied(true)
                .usedTools(List.of())
                .toolCallCount(0)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private AiChatResponse unavailableResponse() {
        return AiChatResponse.builder()
                .answer(unavailableMessage)
                .model(aiModel)
                .denied(false)
                .usedTools(List.of())
                .toolCallCount(0)
                .generatedAt(LocalDateTime.now())
                .build();
    }
}