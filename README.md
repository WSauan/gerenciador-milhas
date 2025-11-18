# ✈️ Gerenciador de Milhas e Pontos (Backend)

API REST desenvolvida para a disciplina de **Programação Web 1** do curso de Bacharelado em Sistemas de Informação (IFS).

O objetivo do sistema é auxiliar usuários no controle de seus cartões de crédito, programas de fidelidade e aquisições, oferecendo cálculo automático de pontos e previsibilidade de crédito.

> **Status do Projeto:** 🚀 Backend Finalizado | ⏳ Frontend (Em breve)

## 👥 Autores (Equipe)

* **Welber Sauan**
* **Juan Wesley**
* **Vicente Loiola**

---

## 🛠️ Tecnologias Utilizadas

O projeto foi construído utilizando as melhores práticas de desenvolvimento moderno em Java:

* **Linguagem:** Java 21 (LTS)
* **Framework:** Spring Boot 3
* **Banco de Dados:** PostgreSQL
* **Segurança:** Spring Security + JWT (JSON Web Token)
* **Documentação:** SpringDoc OpenAPI (Swagger UI)
* **Relatórios:** iText (PDF) e Apache Commons CSV
* **Testes:** JUnit 5 e Mockito

---

## ✨ Funcionalidades Principais

### 🔐 Autenticação e Segurança

* Cadastro de usuários com senha criptografada (BCrypt).
* Login seguro retornando Token JWT.
* Recuperação de senha (solicitação e reset via token).
* Atualização de perfil de usuário.

### 💳 Gestão de Cartões

* Cadastro de Bandeiras (ex: Visa, Mastercard).
* Cadastro de Programas de Pontos (ex: Smiles, TudoAzul).
* Cadastro de Cartões com **Fator de Conversão** personalizado (pontos por real/dólar).

### 🛍️ Aquisições e Pontos

* Registro de compras (Aquisições).
* **Cálculo Automático:** O sistema calcula os pontos baseados no valor gasto e no fator do cartão.
* **Upload de Comprovantes:** Suporte para anexo de arquivos (PDF/Imagem) na aquisição.

### 📊 Dashboard e Relatórios

* Indicadores de saldo de pontos por cartão.
* Cálculo de prazo médio de recebimento de pontos.
* **Exportação de Dados:** Histórico completo disponível para download em **CSV** e **PDF**.

---

## 🚀 Como Executar o Projeto

### Pré-requisitos

* Java 21 instalado.
* Maven instalado.
* PostgreSQL instalado e rodando.

### Passo 1: Configuração do Banco de Dados

Crie um banco de dados no PostgreSQL com o nome `milhas_db`:

```sql
CREATE DATABASE milhas_db;
```

### Passo 2: Configuração da Aplicação

Verifique o arquivo src/main/resources/application.properties e ajuste seu usuário e senha do banco, se necessário:

spring.datasource.username=postgres
spring.datasource.password=sua_senha_aqui

### Passo 3: Rodar a Aplicação

execute através da sua IDE (VS Code, IntelliJ, Eclipse) rodando a classe GerenciadorMilhasApplication.java ou via terminal na raiz do projeto:
mvn spring-boot:run

## 📚 Documentação e Testes (Swagger UI)

A API possui documentação interativa via **Swagger/OpenAPI**. Você pode testar todos os endpoints diretamente pelo navegador, sem instalar nada.

👉 **Acesse:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### 🧪 Roteiro de Teste Completo (Passo a Passo)

Siga esta ordem para demonstrar todas as funcionalidades do sistema pois maioria dos endpoints é protegida

#### 1. Autenticação e Cadastro

* Abra `usuario-controller` > `POST /api/usuarios`.
* Clique em **Try it out**.
* Cole o JSON abaixo e clique em **Execute**:

    ```json
    {
      "nome": "Usuario Demo",
      "email": "demo@teste.com",
      "senha": "123"
    }
    ```

* Abra `autenticacao-controller` > `POST /api/login`.
* Clique em **Try it out**.
* Cole o JSON abaixo e clique em **Execute**:

    ```json
    {
      "email": "demo@teste.com",
      "senha": "123"
    }
    ```

* **Copie o token** gerado no corpo da resposta (a string longa dentro de `"token": "..."`).

#### 2. Autorizar (Liberar Cadeado)

* Role até o topo da página e clique no botão verde **Authorize** (🔓).
* No campo *Value*, digite: `Bearer` seguido do token colado.
  * *Exemplo:* `Bearer eyJhbGciOiJIUzI1Ni...`
* Clique em **Authorize** e depois em **Close**.
* 🔒 Os cadeados ao lado dos endpoints ficarão fechados.

#### 3. Configuração de Cartões

* **Cadastrar Bandeira:** Vá em `bandeira-controller` > `POST` e execute:

    ```json
    { "nome": "Visa" }
    ```

* **Cadastrar Programa:** Vá em `programa-de-pontos-controller` > `POST` e execute:

    ```json
    { "nome": "Livelo" }
    ```

* **Cadastrar Cartão:** Vá em `cartao-controller` > `POST`. Observe o `fatorConversao` (multiplicador). Use os IDs gerados (geralmente 1):

    ```json
    {
      "nome": "Visa Infinite Demo",
      "saldoDePontos": 0,
      "fatorConversao": 2.5,
      "bandeiraId": 1,
      "programaId": 1
    }
    ```

#### 4. Registro de Aquisição (Com Upload)

Teste o endpoint `multipart/form-data` que calcula pontos automaticamente:

* Vá em `aquisicao-controller` > `POST /api/aquisicoes`.
* No campo **`aquisicao`** (JSON), cole:

    ```json
    {
      "descricao": "Compra Notebook",
      "valorGasto": 1000,
      "dataCompra": "2025-11-01",
      "dataPrevistaCredito": "2025-12-01",
      "cartaoId": 1
    }
    ```

* No campo **`comprovante`**, clique no botão para selecionar um arquivo PDF ou Imagem do seu computador.
* Clique em **Execute**.
* **Verifique a resposta:** O sistema deve retornar `pontosCalculados: 2500` (1000 * 2.5).

#### 5. Dashboard e Relatórios

Visualize os dados gerados e teste o download:

* **Pontos por Cartão:** Vá em `dashboard-controller` > `GET /api/dashboard/pontos-por-cartao` e clique em **Execute**.
* **Prazo Médio:** Vá em `dashboard-controller` > `GET /api/dashboard/prazo-medio-recebimento` e clique em **Execute**.
* **Baixar Relatório PDF:** Vá em `dashboard-controller` > `GET /api/dashboard/exportar-historico-pdf`.
  * Clique em **Execute**.
  * Clique no link **"Download file"** que aparecerá na resposta para baixar o arquivo.

#### 6. Gestão de Usuário (Extras)

* **Atualizar Perfil:** Vá em `usuario-controller` > `PUT /api/usuarios/perfil`.

    ```json
    { "nome": "Usuario Demo Atualizado" }
    ```

* **Recuperar Senha:**
  * Use `POST /api/forgot-password` com o e-mail para gerar o token.
  * Use `POST /api/reset-password` com o token gerado e a nova senha.

### 🧪 Testes Automatizados

* O projeto possui testes automatizados para garantir a qualidade do código.
* Testes Unitários: Validam as regras de negócio (ex: cálculo matemático de pontos).
* Testes de Integração: Validam o fluxo completo da API e a segurança.

Para rodar os testes execute o comando:
  **mvn test**

## 📂 Estrutura do Projeto

```text
br.com.milhas.gerenciador
├── config/          # Configurações (Swagger, Security)
├── controller/      # Endpoints da API (RestControllers)
├── dto/             # Objetos de transferência de dados (Records)
├── model/           # Entidades JPA (Banco de Dados)
├── repository/      # Interfaces de acesso a dados
├── security/        # Filtros e Serviços de Token JWT
└── service/         # Regras de Negócio
