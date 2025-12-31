# Copilot Instructions for socius-realtime-service

## Repository Overview

**socius-realtime-service** is a real-time notification service built with Ktor (Kotlin web framework). It provides WebSocket-based real-time communication, consuming messages from RabbitMQ and broadcasting them to connected clients. The service uses JWT authentication and Azure Key Vault for configuration management.

### Repository Statistics
- **Language**: Kotlin 2.2.21
- **Framework**: Ktor 3.3.2 (Netty server)
- **Build Tool**: Gradle 9.2.1 with Kotlin DSL
- **Runtime**: Java 17 (Temurin)
- **Project Size**: Small (~20 Kotlin source files)
- **Main Entry Point**: `src/main/kotlin/Application.kt` (com.uit.ApplicationKt)

### Key Technologies
- **Ktor**: WebSockets, CORS, Call Logging, JWT Auth, Content Negotiation
- **RabbitMQ**: Message broker client (amqp-client 5.28.0)
- **Azure**: Key Vault for secrets, Azure Identity for authentication
- **Serialization**: kotlinx.serialization JSON
- **Logging**: Logback with SLF4J
- **Code Quality**: Spotless with ktlint 1.8.0
- **Packaging**: Shadow plugin for fat JAR creation

## Build & Validation Commands

### Critical Build Information
**IMPORTANT**: The first Gradle command in a clean environment takes 1.5-2 minutes due to daemon initialization and dependency downloads. Subsequent commands are much faster (1-60 seconds). Always allow adequate time for initial runs.

### Environment Setup
1. **Java Version**: Java 17 is REQUIRED. Verify with `java -version`
2. **Gradle Wrapper**: Always use `./gradlew` (Linux/Mac) or `gradlew.bat` (Windows), never use system gradle
3. **No additional setup needed**: Gradle wrapper handles all dependencies automatically

### Build Commands (in order of typical usage)

#### Clean Build Directory
```bash
./gradlew clean
```
- **Time**: ~90 seconds (first run), ~1-5 seconds (subsequent)
- **Purpose**: Removes build artifacts from `build/` directory
- **Note**: Run this if you encounter strange build errors

#### Run Tests
```bash
./gradlew test
```
- **Time**: ~11 seconds (with cached build), ~20-30 seconds (first run after clean)
- **Output**: Test reports in `build/reports/tests/test/index.html`
- **Test Location**: `src/test/kotlin/`
- **Always run tests after making code changes**

#### Build Project (without tests)
```bash
./gradlew build -x test
```
- **Time**: ~55 seconds (first run), ~5-10 seconds (subsequent)
- **Output**: Regular JAR in `build/libs/socius-realtime-service-0.0.1.jar` (81KB)
- **Use this for faster iteration when you've already validated tests**

#### Build Complete Project (with tests)
```bash
./gradlew build
```
- **Time**: ~60 seconds
- **Includes**: Compilation, tests, JAR creation, distribution archives
- **Always run before creating a PR or pushing changes**

#### Create Executable Fat JAR
```bash
./gradlew shadowJar
```
- **Time**: ~10 seconds (if already built)
- **Output**: Fat JAR at `build/libs/socius-realtime-service-all.jar` (~33MB)
- **Contains**: Application + all runtime dependencies
- **Required for**: Production deployments
- **Also created by**: `./gradlew build`

#### Code Formatting Check
```bash
./gradlew spotlessCheck
```
- **Time**: ~1-2 seconds
- **Purpose**: Validates code style (ktlint) without modifying files
- **Fails if**: Code doesn't match style guidelines
- **Always run before committing code**

#### Auto-Format Code
```bash
./gradlew spotlessApply
```
- **Time**: ~1-2 seconds
- **Purpose**: Automatically fixes code style violations
- **Modifies files**: Yes, formats Kotlin files and build scripts
- **Run this if spotlessCheck fails**, then review changes

#### Run Application Locally
```bash
./gradlew run
```
- **Time**: Starts in ~10-15 seconds, runs until stopped
- **Port**: 8081 (configured in Application.kt)
- **Host**: 0.0.0.0
- **Stop**: Ctrl+C
- **Requirements**: Environment variables for RabbitMQ and optionally Azure Key Vault
- **Success message**: "Application started in X seconds" and "Responding at http://0.0.0.0:8081"

### Command Sequences

**For typical development workflow:**
```bash
./gradlew spotlessApply    # Format code
./gradlew test             # Run tests
./gradlew spotlessCheck    # Verify style
./gradlew build            # Full build
```

**For quick iteration:**
```bash
./gradlew test             # Test changes
./gradlew build -x test    # Build without re-running tests
```

**Before pushing changes:**
```bash
./gradlew clean            # Clean slate
./gradlew build            # Full build with tests
./gradlew spotlessCheck    # Verify formatting
```

## Project Architecture

### Directory Structure
```
/
├── .github/
│   └── workflows/
│       └── main.yml           # CI/CD deployment workflow (manual trigger)
├── gradle/
│   ├── libs.versions.toml     # Centralized dependency versions
│   └── wrapper/               # Gradle wrapper files
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   ├── Application.kt           # Main entry point (port 8081)
│   │   │   ├── config/                  # Configuration management
│   │   │   │   ├── ConfigManager.kt     # Env vars + Azure Key Vault
│   │   │   │   ├── JwtConfig.kt         # JWT authentication config
│   │   │   │   └── RabbitMQConfig.kt    # RabbitMQ connection config
│   │   │   ├── enums/                   # Type-safe enums
│   │   │   │   ├── EventTypes.kt        # WebSocket event types
│   │   │   │   └── RoutingType.kt       # Message routing types
│   │   │   ├── model/                   # Data models (all @Serializable)
│   │   │   │   ├── NotificationMessage.kt
│   │   │   │   ├── NotificationPayload.kt
│   │   │   │   └── WebSocketMessage.kt
│   │   │   ├── plugins/                 # Ktor feature configurations
│   │   │   │   ├── HTTP.kt              # CORS configuration
│   │   │   │   ├── Monitoring.kt        # Call logging setup
│   │   │   │   ├── Rounting.kt          # Route definitions
│   │   │   │   ├── Security.kt          # JWT authentication
│   │   │   │   ├── Serialization.kt     # JSON serialization
│   │   │   │   └── Sockets.kt           # WebSocket configuration
│   │   │   ├── rabbitmq/                # RabbitMQ integration
│   │   │   │   └── RabbitMQConsumer.kt  # Message consumer
│   │   │   ├── utils/                   # Utility functions
│   │   │   │   └── LoggingExtensions.kt # Logging helpers
│   │   │   └── websocket/               # WebSocket management
│   │   │       ├── ConnectionManager.kt # Connection pool (thread-safe)
│   │   │       └── WebSocketHandler.kt  # WebSocket handlers
│   │   └── resources/
│   │       └── logback.xml              # Logging configuration
│   └── test/
│       └── kotlin/
│           └── ApplicationTest.kt       # Basic health check test
├── build.gradle.kts                     # Main build configuration
├── settings.gradle.kts                  # Project settings
├── gradle.properties                    # Version properties
├── .editorconfig                        # Editor configuration (used by ktlint)
└── .gitignore                           # Git ignore patterns
```

### Configuration Files

#### build.gradle.kts
- Defines all plugins: kotlin-jvm, ktor, serialization, spotless, dokka, shadow
- Application main class: `com.uit.ApplicationKt`
- Spotless configuration with ktlint rules
- Dependencies bundled via `libs.bundles.ktor.server`
- Shadow JAR configuration (produces fat JAR without version in name)

#### gradle/libs.versions.toml
- **Centralized version catalog** - all dependency versions defined here
- Update versions here, not in build.gradle.kts
- Includes version catalogs for Ktor, Azure, Kotlin, and third-party libraries

#### .editorconfig
- Defines code style: 4 spaces, UTF-8, LF line endings
- Max line length: 120 characters for Kotlin files
- Used by ktlint for style enforcement
- **DO NOT modify** without team consensus

### Key Architectural Components

#### Application Flow
1. **Main Entry**: `Application.kt` starts Netty server on port 8081
2. **Configuration**: `ConfigManager` loads from env vars and/or Azure Key Vault
3. **Security**: JWT authentication configured in `Security.kt`
4. **WebSocket**: Clients connect via `/ws/hub` endpoint (defined in `Sockets.kt`)
5. **RabbitMQ**: Consumer listens for messages and broadcasts to connected WebSocket clients
6. **Connection Management**: `ConnectionManager` tracks active WebSocket sessions (thread-safe)

#### Configuration Management
- **Primary**: Environment variables (for local development)
- **Secondary**: Azure Key Vault (for production)
- **Required Env Vars** (if not using Key Vault):
  - `RABBIT_HOST`, `RABBIT_PORT`, `RABBIT_USERNAME`, `RABBIT_PASSWORD`
  - `RABBIT_VHOST`, `RABBIT_SSL`
  - `RABBITMQ_EXCHANGE_NAME`, `NOTIFICATION_ROUTING_KEY`
  - `AZURE_TENANT_ID`, `AZURE_CLIENT_ID` (optional)
- **Key Vault**: Set `KEY_VAULT_ENDPOINT` to enable Azure Key Vault loading

#### Authentication
- JWT tokens required for WebSocket connections
- Token validation using Nimbus JOSE JWT and Auth0 libraries
- Configuration loaded from `JwtConfig`

## CI/CD Pipeline

### GitHub Actions Workflow (.github/workflows/main.yml)

**Trigger**: Manual workflow dispatch only (not automatic on push/PR)

**Workflow Jobs**:
1. **validate**: Checks user typed "deploy" to confirm
2. **pre-check**: Verifies server disk space (512MB min) and Java 17
3. **build**: 
   - Uses Java 17 (Temurin)
   - Runs: `./gradlew clean shadowJar -x test --parallel --build-cache`
   - Finds fat JAR: `build/libs/*-all.jar`
   - Renames to: `socius-realtime-service.jar`
4. **deploy**: 
   - Deploys to Azure VM via SSH
   - Environment options: staging or production
   - Restarts systemd service: `socius-realtime.service`

**Important Notes**:
- Tests are SKIPPED in CI (`-x test`)
- Build uses `--parallel --build-cache` for performance
- Deployment expects fat JAR at `build/libs/*-all.jar`
- Service runs as systemd unit on target server

### Pre-commit Validation

**There are NO automated pre-commit hooks or PR checks**. Before pushing:
1. Run `./gradlew spotlessCheck` - ensure code style compliance
2. Run `./gradlew test` - ensure all tests pass
3. Run `./gradlew build` - ensure full build succeeds
4. Manually verify changes don't break existing functionality

## Development Guidelines

### Making Code Changes

1. **Always format code**: Run `./gradlew spotlessApply` after editing
2. **Follow existing patterns**: 
   - Use `@Serializable` for data classes
   - Use extension functions for logging: `private val logger = logger()`
   - Kotlin coding conventions as enforced by ktlint
3. **Test changes**: Add or update tests in `src/test/kotlin/`
4. **Configuration**: Never hardcode values, use `ConfigManager`
5. **Error handling**: Log errors appropriately, use structured logging

### Code Style Rules (enforced by ktlint)
- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Line endings**: LF (Unix-style)
- **Final newline**: Required in all files
- **Trailing whitespace**: Not allowed
- **Import ordering**: Kotlin stdlib first, then third-party

### Common Build Issues

**Issue**: "Gradle daemon disappeared unexpectedly"
- **Solution**: Run `./gradlew --stop`, then retry command

**Issue**: "Could not resolve dependencies"
- **Solution**: Check internet connection, clear Gradle cache: `rm -rf ~/.gradle/caches/`

**Issue**: Spotless check fails
- **Solution**: Run `./gradlew spotlessApply` to auto-fix

**Issue**: Tests fail with connection errors
- **Solution**: Tests may require environment setup. Check `ApplicationTest.kt` - uses minimal test module without external deps

**Issue**: Build takes too long (>5 minutes)
- **Solution**: First build always slow. Enable configuration cache (warning message shown) for faster builds

### Dependencies

**Adding New Dependencies**:
1. Add version to `gradle/libs.versions.toml` under `[versions]`
2. Add library to `gradle/libs.versions.toml` under `[libraries]`
3. Reference in `build.gradle.kts`: `implementation(libs.library.name)`
4. Run `./gradlew build` to fetch and test

**Current Major Dependencies**:
- Ktor 3.3.2 (server framework)
- RabbitMQ client 5.28.0
- Azure Key Vault 4.10.4
- Nimbus JOSE JWT 10.5
- Logback 1.5.23

## Working with This Codebase

### File Locations Quick Reference
- **Add route**: `src/main/kotlin/plugins/Rounting.kt`
- **Add WebSocket handler**: `src/main/kotlin/websocket/WebSocketHandler.kt`
- **Modify authentication**: `src/main/kotlin/plugins/Security.kt` and `src/main/kotlin/config/JwtConfig.kt`
- **Add configuration**: `src/main/kotlin/config/ConfigManager.kt`
- **Change logging**: `src/main/resources/logback.xml`
- **Add model**: `src/main/kotlin/model/` (remember `@Serializable`)

### Testing Strategy
- Current tests are minimal (health check only)
- Add tests to `src/test/kotlin/`
- Use `testApplication` from ktor-server-test-host for integration tests
- Run specific test: `./gradlew test --tests "com.uit.ApplicationTest"`

### Debugging Tips
1. **Enable debug logging**: Change `<root level="INFO">` to `DEBUG` in `logback.xml`
2. **View logs**: Server logs to console (STDOUT) in pattern `YYYY-MM-dd HH:mm:ss.SSS`
3. **Test locally**: Ensure environment variables are set before running `./gradlew run`
4. **WebSocket testing**: Use tools like wscat or browser console

## Important Notes

### Trust These Instructions
These instructions have been validated by running each command and verifying the results. If you encounter issues:
1. First, verify you're using the exact commands shown
2. Check that Java 17 is installed and active
3. Ensure you're in the project root directory
4. Only search for additional information if these instructions are incomplete or incorrect

### Performance Expectations
- Clean build (first time): ~90 seconds
- Incremental build: ~5-10 seconds
- Tests: ~11 seconds
- Formatting: ~1-2 seconds
- Shadow JAR: ~10 seconds (after build)

### Deployment Context
This service is deployed to Azure VMs running systemd. The fat JAR is the deployment artifact. Local development doesn't require Docker or systemd.
