package com.demo.talentbridge.enums;

/**
 * Enum representing all supported skills in the platform.
 * Using enum instead of a DB table for type-safety and simplicity.
 * Skills are a well-known, relatively stable set in the tech/job domain.
 */
public enum SkillName {

    // Programming Languages
    JAVA,
    PYTHON,
    JAVASCRIPT,
    TYPESCRIPT,
    CSHARP,
    CPP,
    C,
    GO,
    RUST,
    KOTLIN,
    SWIFT,
    PHP,
    RUBY,
    SCALA,
    R,
    DART,
    MATLAB,

    // Web Frontend
    HTML,
    CSS,
    REACT,
    ANGULAR,
    VUE,
    NEXTJS,
    NUXTJS,
    SVELTE,
    JQUERY,
    BOOTSTRAP,
    TAILWIND_CSS,
    SASS,

    // Web Backend
    SPRING_BOOT,
    SPRING_FRAMEWORK,
    NODEJS,
    EXPRESS,
    DJANGO,
    FLASK,
    FASTAPI,
    LARAVEL,
    RAILS,
    ASP_NET,
    NESTJS,

    // Mobile
    ANDROID,
    IOS,
    REACT_NATIVE,
    FLUTTER,
    XAMARIN,

    // Databases
    MYSQL,
    POSTGRESQL,
    MONGODB,
    REDIS,
    ELASTICSEARCH,
    ORACLE,
    MSSQL,
    SQLITE,
    CASSANDRA,
    DYNAMODB,
    FIREBASE,

    // Cloud & DevOps
    AWS,
    AZURE,
    GCP,
    DOCKER,
    KUBERNETES,
    TERRAFORM,
    ANSIBLE,
    JENKINS,
    GITHUB_ACTIONS,
    GITLAB_CI,
    LINUX,
    NGINX,
    APACHE,

    // Data & AI
    MACHINE_LEARNING,
    DEEP_LEARNING,
    DATA_SCIENCE,
    DATA_ANALYSIS,
    TENSORFLOW,
    PYTORCH,
    PANDAS,
    NUMPY,
    SPARK,
    HADOOP,
    TABLEAU,
    POWER_BI,
    SQL,

    // Testing
    JUNIT,
    SELENIUM,
    CYPRESS,
    JEST,
    POSTMAN,
    JMETER,

    // Tools & Practices
    GIT,
    AGILE,
    SCRUM,
    JIRA,
    FIGMA,
    PHOTOSHOP,
    ILLUSTRATOR,

    // Soft Skills / Other
    COMMUNICATION,
    LEADERSHIP,
    PROBLEM_SOLVING,
    TEAMWORK,
    PROJECT_MANAGEMENT,
    BUSINESS_ANALYSIS,
    UI_UX_DESIGN,
    DEVOPS,
    MICROSERVICES,
    REST_API,
    GRAPHQL,
    GRPC,
    KAFKA,
    RABBITMQ,
    WEBSOCKET
}
