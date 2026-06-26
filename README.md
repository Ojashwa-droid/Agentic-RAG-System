# 🤖 Agentic RAG System

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)
![Ollama](https://img.shields.io/badge/Ollama-000000?style=for-the-badge&logo=ollama&logoColor=white)

An advanced, autonomous backend system demonstrating state-of-the-art Generative AI integration using **Spring AI**. This project implements an Agentic Retrieval-Augmented Generation (RAG) architecture capable of intelligently routing queries, retrieving dynamic context, and utilizing local Large Language Models for secure, context-aware reasoning.

---

## 🏗️ System Architecture

The core of the system relies on an intelligent routing mechanism that evaluates user prompts and dynamically decides whether to query the vector database, execute a specific tool/function, or synthesize an ensemble response.

```mermaid
graph TD
    A[Client Request] --> B[Spring Security Filter Chain]
    B --> C[API Gateway / Controller]
    C --> D{Agentic Router}
    D -- General Query --> E[Local LLM via Ollama]
    D -- Context Needed --> F[Vector Database]
    F -->|Retrieve Embeddings| G[Prompt Augmentation]
    G --> E
    E --> H[Response Formulation]
    H --> I[Client Response]

    style A fill:#f9f,stroke:#333,stroke-width:2px
    style E fill:#bbf,stroke:#333,stroke-width:2px
    style F fill:#dfd,stroke:#333,stroke-width:2px
```

---

## ✨ Key Features & Illustrations

### Dynamic Retrieval-Augmented Generation (RAG)

Instead of relying solely on foundational model training data, the system embeds document context in real-time, significantly reducing hallucinations and grounding responses in factual, domain-specific data.

```mermaid
sequenceDiagram
    participant User
    participant SpringApp
    participant VectorStore
    participant LLM
    User->>SpringApp: POST /api/v1/query (prompt)
    SpringApp->>VectorStore: Search Top-K similarities
    VectorStore-->>SpringApp: Return relevant context fragments
    SpringApp->>LLM: Send Augmented Prompt (Context + Query)
    LLM-->>SpringApp: Stream synthesized answer
    SpringApp-->>User: Return contextualized JSON response
```

### Autonomous Agentic Routing

The application acts as an orchestrator, deciding how to handle a prompt; with multiple agents sitting at nodes of each step of RAG - Guardrail Agent (Static, Dynamic for pre & post retrieval), Evaluator Agent.

### Localized AI Inferencing

Integrated with Ollama to run models locally, guaranteeing data privacy, eliminating API latency variations, and keeping token costs at zero during development and localized deployment.

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Core Framework | Java, Spring Boot 3.x |
| AI & Orchestration | Spring AI |
| Security | Spring Security (OAuth2 / JWT) |
| Persistence | Spring Data JPA, PostgreSQL / Vector Database (Qdrant) |
| Containerization | Docker |
| Inference Engine | Ollama |
| API Testing | Postman |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Ollama](https://ollama.ai/) installed and running locally

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Ojashwa-droid/Agentic-RAG-System.git
   cd Agentic-RAG-System
   ```

2. **Pull the required LLM via Ollama:**
   ```bash
   ollama run llama3   # Or your specific model of choice
   ```

3. **Start infrastructure via Docker (Database, Vector Store):**
   ```bash
   docker-compose up -d
   ```

4. **Build and run the Spring Boot application:**
   ```bash
   ./mvnw spring-boot:run
   ```

The application should now be running at `http://localhost:8080` (or your configured port).

---

## 📡 Example Usage

```bash
curl -X POST http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Summarize the key points from the uploaded document"}'
```

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/Ojashwa-droid/Agentic-RAG-System/issues).

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Ojashwa**
GitHub: [@Ojashwa-droid](https://github.com/Ojashwa-droid)
