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
