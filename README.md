===================================================================
   PROMPTS DE IA UTILIZADOS NO DESENVOLVIMENTO
===================================================================

Durante o desenvolvimento deste Web Scraper, utilizei IA (LLM) como um assistente de engenharia em quatro etapas principais, com foco em estruturação, clean code e validação de regras de negócio:

1. ETAPA DE ANÁLISE DO DOM (HTML/CSS)
Prompt utilizado: "Ajude-me a mapear a estrutura HTML e identificar os melhores seletores CSS da página alvo (Stellarcraft). Preciso isolar os elementos de título, marca, skus (com validação de disponibilidade), especificações técnicas e reviews."

2. ETAPA DE ESTRUTURAÇÃO E CLEAN CODE
Prompt utilizado: "Com base na extração do Jsoup, ajude-me a estruturar e refatorar o código Java. Quero utilizar a funcionalidade 'Records' do Java para modelar os dados de forma limpa e imutável, além de adicionar comentários descritivos nos métodos principais."

3. ETAPA DE VALIDAÇÃO DE REQUISITOS DO EDITAL
Prompt utilizado: "Revise rigorosamente as exigências técnicas do edital da Infosimples e cruze com o meu arquivo 'produto.json' gerado. Preciso garantir que a tipagem dos dados (Strings, Floats, nulls), a estrutura de arrays e, principalmente, a nomenclatura exata das chaves (ex: 'specification' no singular) estejam 100% corretas."

4. ETAPA DE BUILD E CONTROLE DE VERSÃO (GIT)
Prompt utilizado: "Revise a estrutura de pastas do meu projeto Maven e gere um arquivo '.gitignore' focado em projetos Java. Faça uma validação de erros para garantir que nenhum arquivo binário executável ou a pasta '/target' suba para o repositório, evitando a desclassificação no processo."
===================================================================