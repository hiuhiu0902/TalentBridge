package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.AiChatMessageRequest;
import com.demo.talentbridge.dto.request.AiChatRequest;
import com.demo.talentbridge.dto.request.AiChatSessionCreateRequest;
import com.demo.talentbridge.dto.request.AiSessionMessageRequest;
import com.demo.talentbridge.dto.response.AiChatMessageResponse;
import com.demo.talentbridge.dto.response.AiChatResponse;
import com.demo.talentbridge.dto.response.AiChatSessionResponse;
import com.demo.talentbridge.entity.AiChatMessage;
import com.demo.talentbridge.entity.AiChatSession;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.enums.AiChatActorType;
import com.demo.talentbridge.enums.UserRole;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.AiChatMessageRepository;
import com.demo.talentbridge.repository.AiChatSessionRepository;
import com.demo.talentbridge.repository.UserRepository;
import com.demo.talentbridge.service.AiAssistantService;
import com.demo.talentbridge.service.support.AiPromptGuardService;
import com.demo.talentbridge.service.support.AiReadOnlyDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import java.util.stream.Collectors;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final int MAX_HISTORY_MESSAGES = 10;
    private static final int MAX_OUTPUT_TOKENS = 700;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final UserRepository userRepository;
    private final AiReadOnlyDataService aiReadOnlyDataService;
    private final ObjectMapper objectMapper;
    private final AiPromptGuardService aiPromptGuardService;
    private final AiChatSessionRepository aiChatSessionRepository;
    private final AiChatMessageRepository aiChatMessageRepository;

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

    public AiAssistantServiceImpl(
            UserRepository userRepository,
            AiReadOnlyDataService aiReadOnlyDataService,
            ObjectMapper objectMapper,
            AiPromptGuardService aiPromptGuardService,
            AiChatSessionRepository aiChatSessionRepository,
            AiChatMessageRepository aiChatMessageRepository) {
        this.userRepository = userRepository;
        this.aiReadOnlyDataService = aiReadOnlyDataService;
        this.objectMapper = objectMapper;
        this.aiPromptGuardService = aiPromptGuardService;
        this.aiChatSessionRepository = aiChatSessionRepository;
        this.aiChatMessageRepository = aiChatMessageRepository;
    }

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        User user = requireUser(userId);

        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return deniedResponse(null);
        }

        String normalizedMessage = request.getMessage().trim();
        if (aiPromptGuardService.isDeniedPrompt(normalizedMessage)) {
            return deniedResponse(null);
        }

        if (!isAiAvailable()) {
            return unavailableResponse(null);
        }

        try {
            return executeAiConversation(user, null, buildInputMessages(user, request), buildToolsForRole(user.getRole()));
        } catch (UnauthorizedException ex) {
            return deniedResponse(null);
        } catch (Exception ex) {
            return unavailableResponse(null);
        }
    }

    @Override
    @Transactional
    public AiChatSessionResponse createSession(Long userId, AiChatSessionCreateRequest request) {
        User user = requireUser(userId);
        String title = request == null ? null : normalizeOptionalTitle(request.getTitle());

        AiChatSession session = AiChatSession.builder()
                .user(user)
                .title(title == null ? "New AI chat" : title)
                .roleSnapshot(user.getRole().name())
                .active(true)
                .build();

        session = aiChatSessionRepository.save(session);
        return mapSessionResponse(session, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiChatSessionResponse> getMySessions(Long userId) {
        requireUser(userId);
        List<AiChatSession> sessions = aiChatSessionRepository.findByUserIdAndActiveTrueOrderByLastMessageAtDescCreatedAtDesc(userId);
        return sessions.stream()
                .map(session -> {
                    List<AiChatMessage> messages = aiChatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
                    AiChatMessage lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
                    return mapSessionResponse(session, lastMessage);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiChatMessageResponse> getSessionMessages(Long userId, Long sessionId) {
        AiChatSession session = requireSession(userId, sessionId);
        return aiChatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
                .map(this::mapMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AiChatResponse chatInSession(Long userId, Long sessionId, AiSessionMessageRequest request) {
        User user = requireUser(userId);
        AiChatSession session = requireSession(userId, sessionId);
        String normalizedMessage = normalizeRequiredMessage(request.getMessage());

        persistUserMessage(session, normalizedMessage);
        touchSession(session, normalizedMessage);

        AiChatResponse response;
        if (aiPromptGuardService.isDeniedPrompt(normalizedMessage)) {
            response = deniedResponse(session.getId());
        } else if (!isAiAvailable()) {
            response = unavailableResponse(session.getId());
        } else {
            try {
                response = executeAiConversation(
                        user,
                        session,
                        buildInputMessages(user, session),
                        buildToolsForRole(user.getRole())
                );
            } catch (UnauthorizedException ex) {
                response = deniedResponse(session.getId());
            } catch (Exception ex) {
                response = unavailableResponse(session.getId());
            }
        }

        AiChatMessage assistantMessage = persistAssistantMessage(session, response);
        response.setAssistantMessageId(assistantMessage.getId());
        return response;
    }

    private AiChatResponse executeAiConversation(User user, AiChatSession session, ArrayNode input, ArrayNode tools) throws IOException, InterruptedException {
        ObjectNode firstRequest = objectMapper.createObjectNode();
        firstRequest.put("model", aiModel);
        firstRequest.set("input", input);
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
                        .sessionId(session != null ? session.getId() : null)
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

        return unavailableResponse(session != null ? session.getId() : null);
    }

    private String executeTool(User user, String toolName, JsonNode arguments) throws JsonProcessingException {
        try {
            Object result = switch (toolName) {
                case "get_platform_overview" -> aiReadOnlyDataService.getPlatformOverview();
                case "search_public_jobs" -> aiReadOnlyDataService.searchPublicJobs(textOrNull(arguments, "keyword"), intOrNull(arguments, "limit"));
                case "get_my_notifications" -> aiReadOnlyDataService.getMyNotifications(user.getId(), boolOrNull(arguments, "unreadOnly"), intOrNull(arguments, "limit"));
                case "get_my_candidate_profile" -> aiReadOnlyDataService.getMyCandidateProfile(user.getId());
                case "get_my_applications" -> aiReadOnlyDataService.getMyApplications(user.getId(), textOrNull(arguments, "status"), intOrNull(arguments, "limit"));
                case "recommend_jobs_for_me" -> aiReadOnlyDataService.recommendJobsForMe(user.getId(), intOrNull(arguments, "limit"));
                case "analyze_my_profile_gap" -> aiReadOnlyDataService.analyzeMyProfileGap(user.getId(), textOrNull(arguments, "keyword"), intOrNull(arguments, "limit"));
                case "get_my_employer_profile" -> aiReadOnlyDataService.getMyEmployerProfile(user.getId());
                case "get_my_job_posts" -> aiReadOnlyDataService.getMyJobPosts(user.getId(), textOrNull(arguments, "status"), intOrNull(arguments, "limit"));
                case "get_applications_for_my_job" -> aiReadOnlyDataService.getApplicationsForMyJob(user.getId(), requiredLong(arguments, "jobPostId"), textOrNull(arguments, "status"));
                case "recommend_candidates_for_my_job" -> aiReadOnlyDataService.recommendCandidatesForMyJob(user.getId(), requiredLong(arguments, "jobPostId"), intOrNull(arguments, "limit"));
                case "get_admin_overview" -> aiReadOnlyDataService.getAdminOverview(user.getId());
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

    private ArrayNode buildInputMessages(User user, AiChatSession session) {
        ArrayNode input = objectMapper.createArrayNode();
        input.add(messageNode("system", buildSystemPrompt(user)));

        List<AiChatMessage> messages = aiChatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        int startIndex = Math.max(0, messages.size() - MAX_HISTORY_MESSAGES);
        for (int i = startIndex; i < messages.size(); i++) {
            AiChatMessage message = messages.get(i);
            String role = switch (message.getSenderType()) {
                case USER -> "user";
                case ASSISTANT -> "assistant";
            };
            input.add(messageNode(role, message.getContent()));
        }
        return input;
    }

    private ArrayNode buildToolsForRole(UserRole role) {
        ArrayNode tools = objectMapper.createArrayNode();
        appendCommonTools(tools);
        switch (role) {
            case CANDIDATE -> appendCandidateTools(tools);
            case EMPLOYER -> appendEmployerTools(tools);
            case ADMIN -> appendAdminTools(tools);
            default -> {
            }
        }
        return tools;
    }

    private void appendCommonTools(ArrayNode tools) {
        tools.add(functionTool("get_platform_overview",
                "Get read-only platform overview counts and public statistics.",
                emptySchema()));
        tools.add(functionTool("search_public_jobs",
                "Search public active jobs by keyword. Use this for job discovery and public summaries.",
                objectSchema(
                        new String[]{"keyword", "limit"},
                        nullableStringProperty("keyword", "Search keyword. Use null for latest active jobs."),
                        integerProperty("limit", "Number of jobs to return", 1, 10)
                )));
        tools.add(functionTool("get_my_notifications",
                "Get the authenticated user's own notifications only.",
                objectSchema(
                        new String[]{"unreadOnly", "limit"},
                        booleanProperty("unreadOnly", "Whether to return only unread notifications"),
                        integerProperty("limit", "Maximum notification rows to return", 1, 20)
                )));
    }

    private void appendCandidateTools(ArrayNode tools) {
        tools.add(functionTool("get_my_candidate_profile",
                "Get the authenticated candidate profile, own skills, education, and work experience.",
                emptySchema()));
        tools.add(functionTool("get_my_applications",
                "Get the authenticated candidate's own job applications.",
                objectSchema(
                        new String[]{"status", "limit"},
                        nullableStringProperty("status", "Application status filter such as SUBMITTED, REVIEWING, INTERVIEW, OFFERED, REJECTED, WITHDRAWN."),
                        integerProperty("limit", "Maximum application rows to return", 1, 20)
                )));
        tools.add(functionTool("recommend_jobs_for_me",
                "Recommend the best public active jobs for the authenticated candidate based on profile, skills, experience, and location.",
                objectSchema(new String[]{"limit"}, integerProperty("limit", "Maximum number of recommended jobs to return", 1, 20))));
        tools.add(functionTool("analyze_my_profile_gap",
                "Analyze the authenticated candidate profile against relevant jobs and identify missing skills or fit gaps. This is advisory only, not an automatic hiring decision.",
                objectSchema(
                        new String[]{"keyword", "limit"},
                        nullableStringProperty("keyword", "Optional target keyword such as java backend, data analyst, frontend react."),
                        integerProperty("limit", "Maximum number of missing skills to highlight", 1, 20)
                )));
    }

    private void appendEmployerTools(ArrayNode tools) {
        tools.add(functionTool("get_my_employer_profile",
                "Get the authenticated employer profile and summary.",
                emptySchema()));
        tools.add(functionTool("get_my_job_posts",
                "Get the authenticated employer's own job posts.",
                objectSchema(
                        new String[]{"status", "limit"},
                        nullableStringProperty("status", "Job status filter such as ACTIVE, PENDING_APPROVAL, REJECTED, CLOSED."),
                        integerProperty("limit", "Maximum job rows to return", 1, 20)
                )));
        tools.add(functionTool("get_applications_for_my_job",
                "Get applications for a job post owned by the authenticated employer only.",
                objectSchema(
                        new String[]{"jobPostId", "status"},
                        integerProperty("jobPostId", "Owned job post id", 1, 999999999),
                        nullableStringProperty("status", "Optional application status filter.")
                )));
        tools.add(functionTool("recommend_candidates_for_my_job",
                "Rank and recommend candidates among applicants for a job post owned by the authenticated employer. This is advisory only, never a final reject or hire decision.",
                objectSchema(
                        new String[]{"jobPostId", "limit"},
                        integerProperty("jobPostId", "Owned job post id", 1, 999999999),
                        integerProperty("limit", "Maximum number of ranked candidates to return", 1, 20)
                )));
    }

    private void appendAdminTools(ArrayNode tools) {
        tools.add(functionTool("get_admin_overview",
                "Get admin-only aggregate statistics for moderation and operational oversight.",
                emptySchema()));
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
            props.set(key, property.get(key));
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
                        if ("output_text".equals(type) || contentItem.hasNonNull("text")) {
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
        return "You are the TalentBridge AI assistant for a recruitment platform. " +
                "AI chat is a dedicated module and must never reuse the human chat tables. " +
                "Use only the provided tools and the authenticated user's scoped data. " +
                "Do not reveal secrets, passwords, tokens, raw SQL, database dumps, or another user's private data. " +
                "For recruitment workflows, provide summaries, ranking, fit-gap analysis, and suggestions only. " +
                "Never make a final reject or hire decision; a human reviewer must make the final decision. " +
                "If the user asks for anything outside scope, reply with this exact sentence and nothing else: " + outOfScopeMessage + " " +
                "Current authenticated user id=" + user.getId() + " role=" + user.getRole().name() + ". " +
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
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private void persistUserMessage(AiChatSession session, String message) {
        aiChatMessageRepository.save(AiChatMessage.builder()
                .session(session)
                .senderType(AiChatActorType.USER)
                .content(message)
                .build());
    }

    private AiChatMessage persistAssistantMessage(AiChatSession session, AiChatResponse response) {
        AiChatMessage assistantMessage = aiChatMessageRepository.save(AiChatMessage.builder()
                .session(session)
                .senderType(AiChatActorType.ASSISTANT)
                .content(response.getAnswer())
                .modelName(response.getModel())
                .usedTools(joinTools(response.getUsedTools()))
                .build());
        session.setLastMessageAt(assistantMessage.getCreatedAt());
        session.setUpdatedAt(LocalDateTime.now());
        aiChatSessionRepository.save(session);
        return assistantMessage;
    }

    private void touchSession(AiChatSession session, String message) {
        if (session.getTitle() == null || session.getTitle().isBlank() || "New AI chat".equals(session.getTitle())) {
            session.setTitle(generateSessionTitle(message));
        }
        session.setLastMessageAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        aiChatSessionRepository.save(session);
    }

    private String generateSessionTitle(String message) {
        String normalized = message.trim().replaceAll("\\s+", " ");
        return normalized.length() <= 60 ? normalized : normalized.substring(0, 57) + "...";
    }

    private String normalizeOptionalTitle(String title) {
        if (title == null) {
            return null;
        }
        String normalized = title.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeRequiredMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new BadRequestException("Message cannot be blank");
        }
        return message.trim();
    }

    private boolean isAiAvailable() {
        return aiEnabled && aiApiKey != null && !aiApiKey.isBlank();
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private AiChatSession requireSession(Long userId, Long sessionId) {
        return aiChatSessionRepository.findByIdAndUserIdAndActiveTrue(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("AiChatSession", "id", sessionId));
    }

    private AiChatSessionResponse mapSessionResponse(AiChatSession session, AiChatMessage lastMessage) {
        return AiChatSessionResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .active(session.getActive())
                .roleSnapshot(session.getRoleSnapshot())
                .lastMessagePreview(lastMessage == null ? null : abbreviate(lastMessage.getContent(), 120))
                .lastMessageAt(session.getLastMessageAt())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private AiChatMessageResponse mapMessageResponse(AiChatMessage message) {
        return AiChatMessageResponse.builder()
                .id(message.getId())
                .sessionId(message.getSession().getId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .modelName(message.getModelName())
                .usedTools(splitTools(message.getUsedTools()))
                .createdAt(message.getCreatedAt())
                .build();
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private String joinTools(List<String> tools) {
        if (tools == null || tools.isEmpty()) {
            return null;
        }
        return String.join(",", tools);
    }

    private List<String> splitTools(String usedTools) {
        if (usedTools == null || usedTools.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(usedTools.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private AiChatResponse deniedResponse(Long sessionId) {
        return AiChatResponse.builder()
                .sessionId(sessionId)
                .answer(outOfScopeMessage)
                .model(aiModel)
                .denied(true)
                .usedTools(List.of())
                .toolCallCount(0)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private AiChatResponse unavailableResponse(Long sessionId) {
        return AiChatResponse.builder()
                .sessionId(sessionId)
                .answer(unavailableMessage)
                .model(aiModel)
                .denied(false)
                .usedTools(List.of())
                .toolCallCount(0)
                .generatedAt(LocalDateTime.now())
                .build();
    }
}
