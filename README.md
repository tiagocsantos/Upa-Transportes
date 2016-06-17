# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 67 - Campus Tagus

O objetivo do projeto de Sistemas Distribuidos é desenvolver um sistema baseado em Web Services, implementados na plataforma Java, para agregação de serviços de transporte de mercadorias à semelhança do que, por exemplo, a Uber efetua para os transportes de passageiros.


-------------------------------------------------------------------------------

## Instruções de instalação


### Ambiente

[0] Iniciar sistema operativo

Windows


[1] Iniciar servidores de apoio

JUDDI:
cd C:\juddi-3.3.2_tomcat-7.0.64_9090\bin
startup.bat

[2] Criar pasta temporária

```
mkdir T_67-project
cd T_67-project/
```


[3] Obter código fonte do projeto (versão entregue)

```
git clone --branch SD_R2 https://github.com/tecnico-distsys/T_67-project.git
```


[4] Instalar módulos de bibliotecas auxiliares

```
cd uddi-naming
mvn clean install

cd ws-handlers
mvn clean install
```

```
cd ...
mvn clean install
```


-------------------------------------------------------------------------------
### Serviço CA
```
cd ca-ws
mvn clean install
mvn exec:java
```

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
cd transporter-ws
mvn clean install
mvn exec:java
mvn exec:java -Dws.i=2
```

[2] Construir **cliente** e executar testes

```
cd transporter-ws-cli
mvn clean install
```

...


-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor**

```
cd broker-ws
mvn clean install
mvn exec:java -Dws.secundary=true
mvn exec:java 
```


[2] Construir **cliente** e executar testes

```
cd broker-ws-cli
mvn clean install exec:java
```

...

-------------------------------------------------------------------------------
**FIM**
