# ✈️ Gerenciador de Milhas e Pontos (Fullstack)

Sistema completo para gestão de cartões de crédito, milhas aéreas e controle financeiro de aquisições. Desenvolvido como requisito da disciplina de **Programação Web 1** do curso de Bacharelado em Sistemas de Informação (IFS).

O projeto integra uma API REST robusta em Java com uma interface Frontend responsiva e moderna, permitindo o cálculo automático de pontos, previsão de crédito baseada em datas e recuperação de acesso via e-mail.

> **Status do Projeto:** 🚀 Concluído (Backend + Frontend Integrados)

## 👥 Equipe de Desenvolvimento

* **Welber Sauan**
* **Juan Wesley**
* **Vicente Loiola**

---

## 🛠️ Tecnologias Utilizadas

### Backend (API)

* **Java 21 (LTS)**
* **Spring Boot 3** (Web, Data JPA, Validation, Security)
* **PostgreSQL** (Banco de Dados)
* **JWT (JSON Web Token)** para autenticação Stateless.
* **JavaMailSender** para envio de e-mails de recuperação.
* **OpenAPI (Swagger UI)** para documentação viva.

### Frontend (Interface)

* **HTML5, CSS3 e JavaScript (ES6+)** puro.
* **Fetch API** para comunicação assíncrona com o Backend.
* **jsPDF** para geração de relatórios no navegador.
* **Design Responsivo** (Mobile-First).

---

## ✨ Funcionalidades Principais

### 🔐 Segurança e Acesso

* **Login Seguro:** Autenticação via Token JWT.
* **Recuperação de Senha via E-mail:** O usuário recebe um token seguro em seu e-mail para redefinir a senha.
* **Criptografia:** Senhas salvas com BCrypt.

### 💳 Gestão Inteligente

* **Dados Pré-carregados:** O sistema já inicia com as principais Bandeiras (Visa, Master) e Programas (Livelo, Esfera) cadastrados.
* **Gestão de Cartões:** Controle de saldo de pontos vinculado ao fator de conversão do cartão.
* **Consistência:** Atualização automática do saldo de pontos ao registrar ou excluir compras.

### 🛍️ Aquisições e Regras de Negócio

* **Cálculo Automático:** Conversão automática de Real para Pontos baseada no cartão.
* **Status Dinâmico:** O sistema define automaticamente se a compra está `PENDENTE` ou `APROVADA` comparando a data atual com a data de previsão de crédito.
* **Estorno de Pontos:** Ao excluir uma compra, os pontos são automaticamente estornados do cartão.
* **Upload de Comprovantes:** Armazenamento de arquivos de comprovante.

### 📊 Relatórios

* **Dashboard:** KPIs de prazo médio e totais.
* **Exportação:** Geração de relatórios em **PDF** (layout paisagem detalhado) e **CSV**.

---

## 📂 Estrutura do Projeto

```text
GERENCIADOR-MILHAS
├── backend/                  # API Spring Boot
│   ├── src/main/java/...     # Código Fonte Java
│   │   ├── config/           # Configurações (Email, Swagger, Security)
│   │   ├── controller/       # Endpoints REST
│   │   ├── dto/              # Objetos de Transferência
│   │   ├── model/            # Entidades e Enums (StatusCredito, etc.)
│   │   ├── service/          # Regras de Negócio (EmailService, Calculos)
│   │   └── Gerenciador...    # Classe Main
│   └── src/main/resources/   # application.properties
│
├── frontend/                 # Interface Web
│   ├── css/                  # Estilos (style.css)
│   ├── js/                   # Lógica (dashboard.js, auth.js)
│   ├── *.html                # Telas (Login, Dashboard, Cadastro)
│   └── assets/               # Imagens e ícones
│
└── README.md                 # Documentação
```

---

## 🚀 Como Executar

### 1. Configuração do Banco de Dados

Certifique-se de ter o PostgreSQL rodando e crie o banco:

```sql
CREATE DATABASE milhas_db;
```

### 2. Executando o Backend

1. Navegue até a pasta `backend`.
2. Configure o arquivo `src/main/resources/application.properties` com suas credenciais de banco e **servidor de e-mail** (ex: Mailtrap ou Gmail App Password).
3. Execute o projeto:

   ```bash
   mvn spring-boot:run
   ```

### 3. Executando o Frontend

1. Não é necessário instalação complexa (NPM/Node).
2. Basta abrir o arquivo `frontend/index.html` no seu navegador.
3. **Recomendado:** Utilize a extensão **"Live Server"** do VS Code para evitar problemas de CORS com arquivos locais.

---

## 🧪 Testando a API (Swagger vs Frontend)

Você pode testar o sistema de duas formas:

### Opção A: Via Frontend (Recomendada)

Acesse `index.html`, crie uma conta e utilize o sistema visualmente. É a forma mais completa de ver as regras de cores (Status Pendente/Aprovado) e gráficos funcionando.

### Opção B: Via Swagger UI (Para Desenvolvedores)

Acesse: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

#### Roteiro de Teste Atualizado (Swagger)

Como o sistema agora envia e-mails e possui dados iniciais, o fluxo mudou levemente:

1. **Login (Autenticação):**
   * Use o endpoint `POST /api/login`.
   * Copie o **Token** gerado.
   * Clique no cadeado **Authorize** no topo e cole o token `Bearer <seu_token>`.

2. **Cadastrar Cartão (Dados Prontos):**
   * Não é necessário cadastrar Bandeiras/Programas manualmente (o sistema já cria).
   * Vá direto em `POST /api/cartoes` e use `bandeiraId: 1` e `programaId: 1`.

3. **Recuperação de Senha (Fluxo de E-mail):**
   * Chame `POST /api/forgot-password` com seu e-mail.
   * **Verifique seu E-mail (ou o console/log da aplicação):** O token não vem mais no corpo da resposta JSON por segurança, ele é enviado para o "Email Service". Pegue o token lá.
   * Chame `POST /api/reset-password` com o token recebido.

4. **Verificar Status Dinâmico:**
   * Crie uma aquisição (`POST /api/aquisicoes`) com `dataPrevistaCredito` no passado.
   * Consulte `GET /api/aquisicoes` e veja que o status retornará automaticamente como **"APROVADO"**.
