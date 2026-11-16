# Como executar o Pitiupi com o plugin Hangman

Este guia mostra como preparar o ambiente, compilar o projeto, instalar
os plugins e iniciar o Pitiupi. Os comandos estão separados por sistema
operacional.

> **Importante:** execute os comandos dentro da pasta raiz do projeto,
> onde existem as pastas `pitiupi` e `plugins`. O ZIP atualizado deve
> estar extraído antes de começar. Não é necessário instalar Git para
> usar o ZIP.

## 1. Ubuntu / Linux (máquina virtual)

### 1.1. Instalar Java 21 e Apache Ant

``` bash
sudo apt update
sudo apt install -y openjdk-21-jdk ant
```

Verifique se as ferramentas estão disponíveis:

``` bash
java -version
javac -version
ant -version
```

Os três comandos devem funcionar. O Java é usado para executar o
programa; o `javac` e o Ant são usados para compilar o código-fonte.

### 1.2. Entrar na pasta do projeto

Ajuste o caminho se o projeto estiver em outro local:

``` bash
cd ~/Downloads/pitiupi-hangman-main
```

Confira se está na pasta correta:

``` bash
ls
```

Devem aparecer, entre outros, `pitiupi` e `plugins`.

### 1.3. Compilar o núcleo e os plugins

Execute um comando por vez e confira se cada compilação termina com
`BUILD SUCCESSFUL`.

**Núcleo do Pitiupi:**

``` bash
ant -f pitiupi/build.xml clean jar
```

**Plugin Chat:**

``` bash
ant -f plugins/chat/build.xml clean jar
```

**Plugin Screencast:**

``` bash
ant -f plugins/screencast/build.xml clean jar
```

**Plugin Hangman:**

``` bash
ant -f plugins/hangman/build.xml clean jar
```

O diretório `plugins/ping` contém apenas arquivos de
documentação/configuração nesta versão e não precisa ser compilado. Não
existe `plugins/mpchat/build.xml` nesta cópia do projeto.

### 1.4. Copiar os plugins para a pasta de execução

``` bash
mkdir -p .pitiupi/plugins
cp plugins/chat/dist/pitiupi.chat.jar .pitiupi/plugins/chat.jar
cp plugins/screencast/dist/pitiupi.screenCast.jar .pitiupi/plugins/screencast.jar
cp plugins/hangman/dist/pitiupi.hangman.jar .pitiupi/plugins/hangman.jar
```

Confira se os três arquivos foram copiados:

``` bash
ls -l .pitiupi/plugins
```

### 1.5. Executar o Pitiupi

Ainda na raiz do projeto:

``` bash
PITIUPI_HOME="$(realpath .pitiupi)" java -jar pitiupi/dist/pitiupi.jar
```

O Pitiupi deverá abrir carregando os plugins que foram copiados para
`.pitiupi/plugins`.

------------------------------------------------------------------------

## 2. Windows (PowerShell)

Esta seção serve para um computador Windows. É necessário ter Java JDK
21 e Apache Ant instalados.

### 2.1. Instalar Java JDK 21

No PowerShell:

``` powershell
winget install --id EclipseAdoptium.Temurin.21.JDK -e
```

Feche e abra o PowerShell novamente e confira:

``` powershell
java -version
javac -version
```

### 2.2. Instalar Apache Ant

Baixe e extraia o Ant para a pasta do usuário:

``` powershell
Invoke-WebRequest -Uri "https://dlcdn.apache.org/ant/binaries/apache-ant-1.10.18-bin.zip" -OutFile "$env:TEMP\apache-ant.zip"
```

``` powershell
Expand-Archive -Path "$env:TEMP\apache-ant.zip" -DestinationPath "$env:USERPROFILE" -Force
```

Configure o Ant e o Java para a janela atual do PowerShell:

``` powershell
$env:ANT_HOME = "$env:USERPROFILE\apache-ant-1.10.18"; $env:JAVA_HOME = Split-Path (Split-Path (Get-Command javac).Source -Parent) -Parent; $env:Path = "$env:ANT_HOME\bin;$env:JAVA_HOME\bin;$env:Path"
```

Confira:

``` powershell
ant -version
java -version
javac -version
```

Se fechar o PowerShell, será necessário configurar novamente as
variáveis nessa janela, a menos que elas sejam adicionadas
permanentemente ao Windows.

### 2.3. Entrar na pasta do projeto

Ajuste o caminho conforme o local em que o ZIP foi extraído:

``` powershell
cd "$env:USERPROFILE\Downloads\pitiupi-hangman-main"
```

### 2.4. Compilar o núcleo e os plugins

Execute um comando por vez:

``` powershell
ant -f pitiupi\build.xml clean jar
```

``` powershell
ant -f plugins\chat\build.xml clean jar
```

``` powershell
ant -f plugins\screencast\build.xml clean jar
```

``` powershell
ant -f plugins\hangman\build.xml clean jar
```

Cada compilação deve terminar com `BUILD SUCCESSFUL`.

### 2.5. Copiar os plugins para a pasta de execução

``` powershell
New-Item -ItemType Directory -Force -Path ".pitiupi\plugins" | Out-Null; Copy-Item "plugins\chat\dist\pitiupi.chat.jar" ".pitiupi\plugins\chat.jar" -Force; Copy-Item "plugins\screencast\dist\pitiupi.screenCast.jar" ".pitiupi\plugins\screencast.jar" -Force; Copy-Item "plugins\hangman\dist\pitiupi.hangman.jar" ".pitiupi\plugins\hangman.jar" -Force
```

Confira os arquivos:

``` powershell
Get-ChildItem ".pitiupi\plugins"
```

### 2.6. Executar o Pitiupi

``` powershell
$env:PITIUPI_HOME = (Resolve-Path ".pitiupi").Path; java -jar ".\pitiupi\dist\pitiupi.jar"
```

------------------------------------------------------------------------

## 3. Como iniciar uma partida de Forca

1.  Inicie o Pitiupi nos dois computadores.
2.  Confira se os dois usuários aparecem como online um para o outro.
3.  No computador que será o desafiante, clique com o botão direito no
    usuário adversário.
4.  Escolha **Convidar para jogar Forca**.
5.  No outro computador, aceite o convite.
6.  O desafiante escolhe a palavra secreta.
7.  O adivinhador envia letras e acompanha as atualizações até a vitória
    ou derrota.

Cada computador precisa ter o projeto atualizado, compilado e o JAR do
Hangman copiado para sua própria pasta `.pitiupi/plugins`.

## 4. Testes de rede e problemas comuns

### Verificar o endereço IP

No Ubuntu:

``` bash
hostname -I
```

No Windows:

``` powershell
ipconfig
```

As duas máquinas precisam ter conectividade entre si. Em uma rede local,
teste o endereço IP do outro computador:

Ubuntu:

``` bash
ping -c 4 IP_DO_OUTRO_COMPUTADOR
```

Windows:

``` powershell
ping IP_DO_OUTRO_COMPUTADOR
```

Substitua `IP_DO_OUTRO_COMPUTADOR` pelo endereço real da outra máquina.

### Verificar se a porta 5000 está ocupada

No Windows:

``` powershell
Get-NetTCPConnection -LocalPort 5000 -ErrorAction SilentlyContinue | Select-Object LocalAddress, LocalPort, State, OwningProcess
```

Para identificar o processo pelo PID:

``` powershell
Get-Process -Id NUMERO_DO_PID
```

No Ubuntu:

``` bash
sudo ss -lntup | grep ':5000'
```

Não encerre processos sem identificar o que são. Se a porta estiver
ocupada, feche corretamente a instância anterior do Pitiupi ou
identifique o serviço responsável antes de tentar novamente.

## 5. Observações importantes

-   Alterar arquivos `.java` não atualiza automaticamente os arquivos
    `.jar`. Depois de alterar o código, compile novamente e copie os
    JARs atualizados para `.pitiupi/plugins`.
-   O ZIP do código-fonte não inclui os JARs compilados, por isso é
    necessário executar os passos de compilação em cada computador.
-   O diretório `.pitiupi` contém os plugins instalados para execução.
    Ele não é a pasta de código-fonte.
-   O plugin Hangman usa o sistema de convites e a comunicação do
    Pitiupi. Por isso, os dois participantes precisam estar executando
    versões compatíveis do projeto e do plugin.
-   Se algum comando de compilação falhar, pare nessa etapa e verifique
    a mensagem de erro antes de continuar.
