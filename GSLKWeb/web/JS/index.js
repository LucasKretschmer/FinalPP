// SCRIPT-GERAL //
function init() {
    document.querySelector('.h1Entrar').addEventListener('click', getTela);
    document.querySelector('.h1Cadastrese').addEventListener('click', getTela);
    document.querySelector('#btnFechaTelaLoguin').addEventListener('click', fecharTela);
    document.querySelectorAll('#header_btnEntrar')[0].addEventListener('click', abrirTela);
    document.querySelectorAll('#header_btnEntrar')[1].addEventListener('click', abrirTela);
    document.querySelectorAll("#header_btnHome")[0].addEventListener('click', irHome);
    document.querySelectorAll("#header_btnHome")[1].addEventListener('click', irHome);
    document.querySelector(".header-mid-logo").addEventListener('click', irHome);
    document.querySelectorAll("#btn_usuario_conta_nome")[0].addEventListener('click', irHome);
    document.querySelectorAll("#btn_usuario_conta_nome")[1].addEventListener('click', irHome);
    document.querySelectorAll("#header_btnContato")[0].addEventListener('click', irContato);
    document.querySelectorAll("#header_btnContato")[1].addEventListener('click', irContato);
    document.querySelectorAll("#btn_usuario_conta_conta")[0].addEventListener('click', irConta);
    document.querySelectorAll("#btn_usuario_conta_conta")[1].addEventListener('click', irConta);
    document.querySelectorAll("#header_btnContato")[0].addEventListener('click', irContato);
    document.querySelectorAll("#header_btnContato")[1].addEventListener('click', irContato);
    document.querySelector("#btnEntrar").addEventListener('click', fazerLogin);
    document.querySelector("#listaOpcoes_fatura").addEventListener('click', mudaAbaOpcoesFatu);
    document.querySelector("#listaOpcoes_config").addEventListener('click', mudaAbaOpcoesConf);
    document.querySelector("#listaOpcoes_dependente").addEventListener('click', mudaAbaOpcoesDep);
    document.querySelector("#listaOpcoes_reserva").addEventListener('click', mudaAbaOpcoesRes);
    document.querySelector("#btn_editar_opcoes").addEventListener('click', editarOpcoes);
    document.querySelector("#btn_salvar_opcoes").addEventListener('click', salvarOpcoes);
    document.querySelector("#btn_editar_senha_opcoes").addEventListener('click', editarSenhaOpcoes);
    document.querySelector("#btn_salvar_senha_opcoes").addEventListener('click', salvarSenhaOpcoes);
    document.querySelector("#btnCadastrar").addEventListener('click', fazerCadastro);
    document.querySelector("#btnfechatela").addEventListener('click', fechaTelaSelectPlano);
    document.querySelector("#contato-btnenviar").addEventListener('click', contatoFeedback);
    document.querySelector("#btn_salvar_plano").addEventListener('click', contrataPlano);
    document.querySelectorAll("#btn_usuario_conta_sair")[0].addEventListener('click', fazerDeslogin);
    document.querySelectorAll("#btn_usuario_conta_sair")[1].addEventListener('click', fazerDeslogin);
    document.querySelectorAll("#btnAvancaCad")[0].addEventListener('click', avancaCad);
    document.querySelectorAll("#btnAvancaCad")[1].addEventListener('click', avancaCad);
    document.querySelectorAll("#btnRetornaCad")[0].addEventListener('click', retornaCad);
    document.querySelectorAll("#btnRetornaCad")[1].addEventListener('click', retornaCad);
    document.querySelector("#closePDF").addEventListener('click', fechaPDF);
    document.querySelector("#btn_inserir_Dependente").addEventListener('click', inserirDependente);
    document.querySelector("#btn_editar_Dependente").addEventListener('click', editarDependente);
    document.querySelector("#btn_salvar_Dependente").addEventListener('click', salvarDependente);
    document.querySelector("#btn_excluir_Dependente").addEventListener('click', excluirDependente);

    if (verificaLogado()) {
        var cod = getCookie("cod");
        if (cod !== false) {
            executaServico("GSLKJava", "login", "verificaLogado", function (data) {
                if (data !== "") {
                    preencheDadosCliente(cod);
                    retornaDuplicatas(cod);
                }
            }, null, "&COD=" + cod + "&EMAIL=" + getCookie("email"));
        }
    }

    setInterval(function () {
        document.querySelector(".next").click();
    }, 3500);
    executaServico("GSLKJava", "planos", "retornarPlanos",
            function (data) {
                if (data[0].STATUS) {
                    montaPlanos(data);
                    var quantPlanos = document.querySelectorAll("#main-planos-button").length;
                    for (var k = 0; k < quantPlanos; k++) {
                        document.querySelectorAll("#main-planos-button")[k].addEventListener('click', apresentaPlano);
                    }
                }
            }, function (erro) {
        alert("Erro na requisição:" + erro);
    }, "");
    setTimeout(function () {
        executaServico("GSLKJava", "buscaDados", "buscaTiposPagameno",
                function (data) {
                    for (var x = 0; x < data.length; x++) {
                        var pagamentos =
                                '  <div class="contrataPlano-paga exemplo2">'
                                + '    <input type="radio" class="cursorPointer" id="p_' + data[x].CPAGAMENTO + '" name="tipo" value="' + data[x].NOME + '">'
                                + '    <label for="p_' + data[x].CPAGAMENTO + '" class="cursorPointer"> ' + data[x].NOME + '</label>'
                                + '</div>';
                        document.querySelector(".divTiposPagamento").innerHTML += pagamentos;
                    }

                }, function (erro) {
            alert("Erro na requisição:" + erro, "Atenção!");
        }, "");
    }, 500);
}

function fazerLogin() {
    var email = document.querySelector('#emailLogin').value;
    var cnpj = document.querySelector('#cnpjLogin').value;
    var senha = document.querySelector('#senhaLogin').value;
    buscaLogin(email, cnpj, senha);
}

function buscaLogin(email, cnpj, senha) {
    if (email && cnpj && senha) {
        executaServico("GSLKJava", "login", "login", function (data) {
            var email = document.querySelector("#emailLogin").value;
            logar(cnpj, data.cliforenduser, preencheDadosCliente(data.cliforenduser, getCookie(email)), email);

            document.querySelector("#emailLogin").value = "";
            document.querySelector("#senhaLogin").value = "";
            document.querySelector("#cnpjLogin").value = "";
            document.querySelector("#btnFechaTelaLoguin").click();
            document.querySelectorAll("#btn_usuario_conta")[0].classList.remove("user-inVisivel");
            document.querySelectorAll("#btn_usuario_conta")[1].classList.remove("user-inVisivel");
            document.querySelectorAll("#header_btnEntrar")[0].classList.add("user-inVisivel");
            document.querySelectorAll("#header_btnEntrar")[1].classList.add("user-inVisivel");

            retornaDuplicatas(data.cliforenduser);
        }, function (erro) {
            alert(erro);
        }, '&tipologin=cliente&ecommerce=S&usuario=' + email + '&cnpj=' + cnpj + '&senha=' + senha);
    }
}

function fazerCadastro() {
    var nomeCad = document.querySelector('#nomeCad').value;
    var telefoneCad = document.querySelector('#telefoneCad').value;
    var emailCad = document.querySelector('#emailCad').value;
    var senhaCad = document.querySelector('#senhaCad').value;
    var cgcCad = document.querySelector('#cgcCad').value;
    var enderecoCad = document.querySelector('#enderecoCad').value;
    var cepCad = document.querySelector('#cepCad').value;
    var numeroCad = document.querySelector('#numeroCad').value;
    var bairroCad = document.querySelector('#bairroCad').value;
    var complementoCad = document.querySelector('#complementoCad').value;
    var cidadeCad = document.querySelector('#cidadeCad').value;
    var ufCad = document.querySelector('#ufCad').value;
    var nascimentoCad = document.querySelector("#nascimentoCad").value.split("-").reverse().join("/");
    if (nomeCad && cgcCad && cepCad && cidadeCad && telefoneCad && emailCad && senhaCad) {
        executaServico('GSLKJava', 'login', 'registraCliente', function (data) {
            buscaLogin(emailCad, cgcCad, senhaCad);
            document.querySelector('#nomeCad').value = "";
            document.querySelector('#telefoneCad').value = "";
            document.querySelector('#emailCad').value = "";
            document.querySelector('#senhaCad').value = "";
            document.querySelector('#cgcCad').value = "";
            document.querySelector('#enderecoCad').value = "";
            document.querySelector('#cepCad').value = "";
            document.querySelector('#numeroCad').value = "";
            document.querySelector('#bairroCad').value = "";
            document.querySelector('#complementoCad').value = "";
            document.querySelector('#cidadeCad').value = "";
            document.querySelector('#ufCad').value = "";
            document.querySelector("#nascimentoCad").value = "";
        }, function (erro) {
            alert(erro);
        }, '&nomeCad=' + nomeCad
                + '&cgcCad=' + cgcCad
                + '&cepCad=' + cepCad
                + '&enderecoCad=' + enderecoCad
                + '&numeroCad=' + numeroCad
                + '&bairroCad=' + bairroCad
                + '&complementoCad=' + complementoCad
                + '&cidadeCad=' + cidadeCad
                + '&ufCad=' + ufCad
                + '&telefoneCad=' + telefoneCad
                + '&emailCad=' + emailCad
                + '&senhaCad=' + senhaCad
                + '&nascimentoCad=' + nascimentoCad
                );
    } else {
        alert('Preencha todos os campos obrigatórios!');
    }
}

function inserirDependente(e) {
    document.querySelector("#nomeDependente").removeAttribute("disabled");
    document.querySelector("#grauDependente").removeAttribute("disabled");
    document.querySelector("#dataDependente").removeAttribute("disabled");
    document.querySelector("#localDependente").removeAttribute("disabled");
    document.querySelector("#ufDependente").removeAttribute("disabled");
    document.querySelector("#cpfDependente").removeAttribute("disabled");

    document.querySelector("#nomeDependente").value = "";
    document.querySelector("#grauDependente").value = "";
    document.querySelector("#dataDependente").value = "";
    document.querySelector("#localDependente").value = "";
    document.querySelector("#ufDependente").value = "";
    document.querySelector("#cpfDependente").value = "";
}

function editarDependente() {
    document.querySelector("#nomeDependente").removeAttribute("disabled");
    document.querySelector("#grauDependente").removeAttribute("disabled");
    document.querySelector("#dataDependente").removeAttribute("disabled");
    document.querySelector("#localDependente").removeAttribute("disabled");
    document.querySelector("#ufDependente").removeAttribute("disabled");
    document.querySelector("#cpfDependente").removeAttribute("disabled");
}

function salvarDependente() {
    var nome = document.querySelector("#nomeDependente").value;
    var grau = document.querySelector("#grauDependente").value;
    var data = document.querySelector("#dataDependente").value;
    data = data.split("-");
    data = data.reverse();
    data = data.join("/");
    var local = document.querySelector("#localDependente").value;
    var uf = document.querySelector("#ufDependente").value;
    var cpf = document.querySelector("#cpfDependente").value;

    executaServico("GSLKJava", "dependentes", "addDependente",
            function (data) {
                document.querySelector("#nomeDependente").value = "";
                document.querySelector("#grauDependente").value = "";
                document.querySelector("#dataDependente").value = "";
                document.querySelector("#localDependente").value = "";
                document.querySelector("#ufDependente").value = "";
                document.querySelector("#cpfDependente").value = "";
                document.querySelector("#nomeDependente").setAttribute("disabled", true);
                document.querySelector("#grauDependente").setAttribute("disabled", true);
                document.querySelector("#dataDependente").setAttribute("disabled", true);
                document.querySelector("#localDependente").setAttribute("disabled", true);
                document.querySelector("#ufDependente").setAttribute("disabled", true);
                document.querySelector("#cpfDependente").setAttribute("disabled", true);
                alert(data);
            }, function (erro) {
        alert("Erro na requisição: " + erro, "Atenção!");
    }, "&CCLIFOR=" + getCookie("cod") + "&NOME=" + nome + "&GRAU=" + grau + "&DATA=" + data + "&CIDADE=" + local + "&UF=" + uf + "&CPF=" + cpf);
    mudaAbaOpcoesDep();
}

function excluirDependente() {
    var tr = document.querySelector(".lineSelected");
    var clifordep = tr.getAttribute("ccliforfilho");
    var clifor = tr.getAttribute("cclifor");

    executaServico("GSLKJava", "dependentes", "apagaDep",
            function (data) {
                alert(data);
            }, function (erro) {
        alert("Erro na requisição:" + erro, "Atenção!");
    }, "&CLIFOR=" + clifor + "&CLIFORDEP=" + clifordep);
    mudaAbaOpcoesDep();
}

function avancaCad(e) {
    if (document.querySelector(".divCadastroTransition").style.left === "-25em") {
        document.querySelector(".divCadastroTransition").style.left = "-50em";
    } else {
        document.querySelector(".divCadastroTransition").style.left = "-25em";
    }
}
function retornaCad(e) {
    if (document.querySelector(".divCadastroTransition").style.left === "-25em") {
        document.querySelector(".divCadastroTransition").style.left = "0";
    } else {
        document.querySelector(".divCadastroTransition").style.left = "-25em";
    }
}

function contatoFeedback() {
    var nome = document.querySelector("#contato-nome").value;
    var idade = document.querySelector("#contato-idade").value;
    var email = document.querySelector("#contato-email").value;
    var texto = document.querySelector("#contato-textarea").value;
    executaServico("GSLKJava", "contato", "contato",
            function (data) {
                document.querySelector("#contato-nome").value = "";
                document.querySelector("#contato-idade").value = "";
                document.querySelector("#contato-email").value = "";
                document.querySelector("#contato-textarea").value = "";
                document.querySelector("#header_btnHome").click();
                alert("Agradecemos pelo seu feedback!", "Sucesso!");
            }, function (erro) {
        alert("Erro na requisição: " + erro, "Atenção!");
    }, "&NOME=" + nome + "&IDADE=" + idade + "&EMAIL=" + email + "&COMENTARIO=" + texto);
}

function contrataPlano(e) {
    var cplano = document.querySelector(".contrataPlano-valor").id.split("_")[0];
    var cuser = document.querySelector(".contrataPlano-valor").id.split("_")[1];
    var cpagamento;
    for (var i = 0; i < document.getElementsByName("tipo").length; i++) {
        if (document.getElementsByName("tipo")[i].checked) {
            cpagamento = document.getElementsByName("tipo")[i].id.split("_")[1];
        }
    }
    var data = new Date();
    var dataa = data.getDate() + "." + data.getMonth() + "." + data.getFullYear();
    var parametros = "&CUSER=" + cuser + "&CPLANO=" + cplano + "&CPAGAMENTO=" + cplano + "&DATA=" + dataa;
    if (cplano !== undefined && cplano !== "" && cplano !== 0) {
        if (cpagamento !== undefined && cpagamento !== "" && cpagamento !== 0) {
            if (cuser !== "" && cuser !== null) {
//projeto, classe, metodo, funcaoOK, funcaoErro, parametros
                executaServico("GSLKJava", "contrataPlano", "contrataPlano",
                        function (data) {
                            if (data.STATUS) {
                                preencheDadosCliente(cuser);
                                document.querySelector(".tampaBackPromocao").classList.add("cont-inVisivel");
                                alert(data.MSG, "Muito bem!");
                            } else {
                                alert("Você tem informações não preenchidas para poder fazer a sua assinatura! \r\n Verifique nas configurações as informações que estão faltando...", "Atenção!");
                            }
                        }, function (erro) {
                    alert("Erro na requisição:" + erro);
                }, parametros);
                alert("Sua assinatura foi concluída com sucesso!\r\nConfira seu email com as informações do plano contratado...", "Muito bem!");
                preencheDadosCliente(cuser);
                document.querySelector(".tampaBackPromocao").classList.add("cont-inVisivel");
            } else {
                document.querySelector("#header_btnEntrar").click();
                alert("Você deve fazer o loguin antes de contratar um plano!");
            }
        }
    }
}

function editarOpcoes() {
    document.querySelector("#nome").removeAttribute("disabled");
    document.querySelector("#emaill").removeAttribute("disabled");
    document.querySelector("#cep").removeAttribute("disabled");
    document.querySelector("#endereco").removeAttribute("disabled");
    document.querySelector("#bairro").removeAttribute("disabled");
    document.querySelector("#cidade").removeAttribute("disabled");
    document.querySelector("#telefone").removeAttribute("disabled");
    document.querySelector("#celular").removeAttribute("disabled");
    document.querySelector("#cpf").removeAttribute("disabled");
    document.querySelector("#num").removeAttribute("disabled");
}

function salvarOpcoes() {
    document.querySelector("#nome").setAttribute("disabled", true);
    document.querySelector("#cep").setAttribute("disabled", true);
    document.querySelector("#emaill").setAttribute("disabled", true);
    document.querySelector("#endereco").setAttribute("disabled", true);
    document.querySelector("#bairro").setAttribute("disabled", true);
    document.querySelector("#cidade").setAttribute("disabled", true);
    document.querySelector("#telefone").setAttribute("disabled", true);
    document.querySelector("#celular").setAttribute("disabled", true);
    document.querySelector("#cpf").setAttribute("disabled", true);
    document.querySelector("#num").setAttribute("disabled", true);
    var nome = document.querySelector("#nome").value;
    var emaill = document.querySelector("#emaill").value;
    var cep = document.querySelector("#cep").value;
    var endereco = document.querySelector("#endereco").value;
    var bairro = document.querySelector("#bairro").value;
    var cidade = document.querySelector("#cidade").value;
    var telefone = document.querySelector("#telefone").value;
    var celular = document.querySelector("#celular").value;
    var cpf = document.querySelector("#cpf").value;
    var cod = document.querySelector("#cclifor").value;
    var num = document.querySelector("#num").value;
    var parametros = "&NOME=" + nome + "&EMAIL=" + emaill + "&CEP=" + cep + "&ENDERECO=" + endereco + "&BAIRRO=" + bairro
            + "&CIDADE=" + cidade + "&TELEFONE=" + telefone + "&CELULAR=" + celular + "&CPF=" + cpf + "&COD=" + cod
            + "&NUMERO=" + num;
    //projeto, classe, metodo, funcaoOK, funcaoErro, parametros
    executaServico("GSLKJava", "salvarDados", "salvarDados",
            function (data) {
                alert(data, "Atenção!");
            }, function (erro) {
        alert("Erro na requisição:" + erro, "Atenção!");
    }, parametros);
}

function editarSenhaOpcoes() {
    document.querySelector("#emaill").removeAttribute("disabled");
    document.querySelector("#senhaa").removeAttribute("disabled");
    document.querySelector("#confSenha").removeAttribute("disabled");
}

function salvarSenhaOpcoes() {
    document.querySelector("#emaill").setAttribute("disabled", true);
    document.querySelector("#senhaa").setAttribute("disabled", true);
    document.querySelector("#confSenha").setAttribute("disabled", true);
    var email = document.querySelector("#emaill").value;
    var senha = document.querySelector("#senhaa").value;
    var confsenha = document.querySelector("#confSenha").value;
    var cclifor = document.querySelector("#cclifor").value;
    executaServico("GSLKJava", "contato", "alteraSenha",
            function (data) {
                if (data) {
                    alert("Sua senha foi redefinida!", "Sucesso");
                } else {
                    alert("Os campos de senha e confirmação da senha devem ser iguais!", "Atenção");
                }
            }, function (erro) {
        alert("Erro na requisição: \r\n" + erro, "Atenção!");
    }, "&EMAIL=" + email + "&SENHA=" + senha + "&CONFSENHA=" + confsenha + "&COD=" + cclifor);
}

function mudaAbaOpcoesFatu() {
    document.querySelector("#opcoes_fatura").classList.remove("cont-inVisivel");
    document.querySelector("#opcoes_config").classList.add("cont-inVisivel");
    document.querySelector("#opcoes_dependente").classList.add("cont-inVisivel");
    document.querySelector("#opcoes_reserva").classList.add("cont-inVisivel");
}

function mudaAbaOpcoesConf() {
    document.querySelector("#opcoes_fatura").classList.add("cont-inVisivel");
    document.querySelector("#opcoes_config").classList.remove("cont-inVisivel");
    document.querySelector("#opcoes_dependente").classList.add("cont-inVisivel");
    document.querySelector("#opcoes_reserva").classList.add("cont-inVisivel");
}

function mudaAbaOpcoesDep() {
    executaServico2("GSLKJava", "dependentes", "atualizaGrid",
            function (data) {
                document.querySelector("#tableDependentes").innerHTML = data;
                var linhaDependente = document.querySelectorAll(".tdDependentes");
                for (var i = 0; i < linhaDependente.length; i++) {
                    linhaDependente[i].addEventListener("click", alteraDadosDependente);
                }
            }, function (erro) {
        alert("Erro na requisição:" + erro);
    }, "&CCLIFOR=" + getCookie("cod") + "&");

    document.querySelector("#opcoes_fatura").classList.add("cont-inVisivel");
    document.querySelector("#opcoes_config").classList.add("cont-inVisivel");
    document.querySelector("#opcoes_dependente").classList.remove("cont-inVisivel");
    document.querySelector("#opcoes_reserva").classList.add("cont-inVisivel");
}

function alteraDadosDependente(e) {
    var tr = e.target;
    while (tr.tagName !== "TR") {
        tr = tr.parentNode;
    }

    var linhaSel = document.querySelectorAll(".lineSelected");
    for (var i = 0; i < linhaSel.length; i++) {
        linhaSel[i].classList.remove("lineSelected");
    }

    tr.classList.add("lineSelected");
    var cliforfilho = tr.getAttribute("ccliforfilho");
    var clifor = tr.getAttribute("cclifor");

    executaServico("GSLKJava", "dependentes", "buscaDadosDep",
            function (data) {
                document.querySelector("#nomeDependente").value = data.NOME;
                document.querySelector("#grauDependente").value = data.DEP;
                document.querySelector("#dataDependente").value = data.DATA.split("/").reverse().join("-");
                document.querySelector("#localDependente").value = data.LOCAL;
                document.querySelector("#ufDependente").value = data.UF;
                document.querySelector("#cpfDependente").value = data.CPF;
            }, function (erro) {
        alert("Erro na requisição:" + erro, "Atenção!");
    }, "&CLIFOR=" + clifor + "&CLIFORDEP=" + cliforfilho);
}

function mudaAbaOpcoesRes() {
    document.querySelector("#opcoes_fatura").classList.add("cont-inVisivel");
    document.querySelector("#opcoes_config").classList.add("cont-inVisivel");
    document.querySelector("#opcoes_dependente").classList.add("cont-inVisivel");
    document.querySelector("#opcoes_reserva").classList.remove("cont-inVisivel");
}

function montaPlanos(data) {
    for (var l = 1; l < data.length; l++) {
        var linhas = ''
                + '<div class="main-planos-container">'
                + '    <div class="hexagono hexagono-medidas">'
                + '        <div class="hexagono-div1">'
                + '            <div class="hexagono-div2">'
                + '                <span> R$' + data[l].VALOR + '</span>'
                + '            </div>'
                + '        </div>'
                + '    </div>'
                + '    <div class="main-planos-mid">'
                + '        <h1 class="main-planos-mid-h1">' + data[l].NOME + '</h1>'
                + '        <p class="main-planos-mid-p1">' + data[l].DESC + '</p><br>'
                + '        <p class="main-planos-mid-p2">Quantidade de pessoas que o plano comporta: ' + data[l].QTDEPESSU + '</p>'
                + '    </div>'
                + '    <div class="plano_' + data[l].COD + '" id="main-planos-button"><span class="plano_' + data[l].COD + '">Assine já</span></div>'
                + '</div>';
        document.querySelector("#main-planos").innerHTML += linhas;
        document.querySelector(".plano_" + data[l].COD).addEventListener("click", apresentaPlano);
    }
}

function apresentaPlano(e) {
    var codPlan = e.target.classList.toString().split("_")[1];
    //projeto, classe, metodo, funcaoOK, funcaoErro, parametros
    executaServico("GSLKJava", "buscaDados", "buscaPromocao",
            function (data) {
                document.querySelector(".contrataPlano-tituloPlano").innerHTML = data.NOME;
                document.querySelector(".contrataPlano-descPlano").innerHTML = data.DESC
                        + '<p class="main-planos-mid-p2">Quantidade de pessoas que o plano comporta: ' + data.QTDEPESSU + '</p>';
                document.querySelector(".tampaBackPromocao").classList.remove("cont-inVisivel");
                document.querySelector(".contrataPlano-valor").innerHTML = "Valor do plano: R$" + data.VALOR;
                document.querySelector(".contrataPlano-valor").id = data.COD + "_" + getCookie("cod");
            }, function (erro) {
        alert("Erro na requisição:" + erro, "Atenção!");
    }, "&CODPLANO=" + codPlan + "&");
}

window.onscroll = function () {
    if (document.documentElement.scrollTop >= 200) {
        document.querySelector(".header-mid-fixo").classList.add("header-mid-fixo-visivel");
    } else {
        document.querySelector(".header-mid-fixo").classList.remove("header-mid-fixo-visivel");
    }
};
function executaServico(projeto, classe, metodo, funcaoOK, funcaoErro, parametros) {
    var http = new XMLHttpRequest();
    if (window.location.href.split("/")[2] === "portal.tecnicon.com.br:7078") {
        http.open('POST', 'http://portal.tecnicon.com.br:7078/TecniconPCHttp/ConexaoHttp?p=evento=ERPMetodos|sessao=|empresa=|filial=|local=|parametro=' +
                'projeto=' + projeto + '|classe=' + classe + '|metodo=' + metodo + '|recurso=metadados' + parametros, true);
    } else {
        http.open('POST', 'http://192.168.1.196:7078/TecniconPCHttp/ConexaoHttp?p=evento=ERPMetodos|sessao=|empresa=|filial=|local=|parametro=' +
                'projeto=' + projeto + '|classe=' + classe + '|metodo=' + metodo + '|recurso=metadados' + parametros, true);
    }

    http.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    http.addEventListener('load', function () {
        if (http.status === 200) {
            var dados = xmlToJSON(http.responseXML);
            if (dados.erro) {
                funcaoErro(dados.erro);
            } else if (dados.result) {
                funcaoOK(dados.result);
            }
        }
    });
    http.send(null);
}

function xmlToJSON(XMLDocument) {
    var retorno = {result: XMLDocument.getElementsByTagName('result')[0].textContent,
        erro: XMLDocument.getElementsByTagName('erro')[0].textContent};
    try {
        retorno.result = JSON.parse(retorno.result);
    } catch (e) {
    }
    try {
        retorno.erro = JSON.parse(retorno.erro);
    } catch (e) {
    }
    return retorno;
}

function executaServico2(projeto, classe, metodo, funcaoOK, funcaoErro, parametros) {
    var http = new XMLHttpRequest();
    if (window.location.href.split("/")[2] === "portal.tecnicon.com.br:7078") {
        http.open('POST', 'http://portal.tecnicon.com.br:7078/TecniconPCHttp/ConexaoHttp?p=evento=ERPMetodos|sessao=|empresa=|filial=|local=|parametro=' +
                'projeto=' + projeto + '|classe=' + classe + '|metodo=' + metodo + '|recurso=metadados' + parametros, true);
    } else {
        http.open('POST', 'http://192.168.1.196:7078/TecniconPCHttp/ConexaoHttp?p=evento=ERPMetodos|sessao=|empresa=|filial=|local=|parametro=' +
                'projeto=' + projeto + '|classe=' + classe + '|metodo=' + metodo + '|recurso=metadados' + parametros, true);
    }

    http.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    http.addEventListener('load', function () {
        if (http.status === 200) {
            if (!http.response.includes("<result>")) {
                var dados = xmlToJSON2(http.responseXML);
            } else {
                var dados = xmlToJSON2(http.response);
            }
            if (dados.erro) {
                funcaoErro(dados.erro);
            } else if (dados.result) {
                funcaoOK(dados.result);
            }
        }
    });
    http.send(null);
}

function xmlToJSON2(XMLDocument) {
    var retorno = {result: XMLDocument.split("</result>")[0].split("<result>")[1],
        erro: XMLDocument.split("</erro>")[0].split("<erro>")[1]};
    try {
        retorno.result;
    } catch (e) {
    }
    try {
        retorno.erro;
    } catch (e) {
    }
    return retorno;
}

function serializeForm(idForm, classCampos) {
    var arrCampos = document.querySelectorAll('#' + idForm + ' .' + classCampos);
    var arrParams = [], i, qtde;
    for (i = 0, qtde = arrCampos.length; i < qtde; i++) {
        arrParams.push(arrCampos[i].id + '=' + encodeURIComponent(arrCampos[i].value));
    }
    return '&' + arrParams.join('&');
}

function fazerDeslogin() {
    document.querySelectorAll("#header_btnEntrar")[0].classList.remove("user-inVisivel");
    document.querySelectorAll("#header_btnEntrar")[1].classList.remove("user-inVisivel");
    document.querySelectorAll("#btn_usuario_conta_nome")[0].innerHTML = '';
    document.querySelectorAll("#btn_usuario_conta_nome")[1].innerHTML = '';
    document.querySelectorAll("#btn_usuario_conta")[0].classList.add("user-inVisivel");
    document.querySelectorAll("#btn_usuario_conta")[1].classList.add("user-inVisivel");
    deslogar();
    document.querySelector("#header_btnHome").click();
}

function retornaDuplicatas(cclifor) {
    executaServico2("GSLKJava", "boletos", "retornaDuplicatas",
            function (data) {
                if (data !== "") {
                    document.querySelector(".boletos").innerHTML = data;
                    var boletos = document.querySelectorAll(".btnPDF");
                    for (var i = 0; i < boletos.length; i++) {
                        boletos[i].addEventListener("click", mostraPDF);
                    }
                } else {
                    document.querySelector(".h1boleto").classList.remove("cont-inVisivel");
                }
            }, function (erro) {
        alert("Erro na requisição:" + erro);
    }, "&CCLIFOR=" + cclifor);
}

function mostraPDF(e) {
    document.querySelector("#opcoes_fatura").classList.add("cont-inVisivel");
    document.querySelector("#dvPDF").classList.remove("cont-inVisivel");
    var params = "&slImpressoras=MATRICIAL&tiporetorno=X&tipoImp=R&numCopias=1&impDuplex=false&RE_PRINT=&&relatorioesp=1528&SRECEBER=" + e.target.parentElement.getAttribute('sreceber') + "&CFILIAL=1&mime=pdf&zoom=100";
    var src = 'http://portal.tecnicon.com.br:7078/Tecnicon/Controller?sessao=-9876&acao=TecniconRelatorioEsp.RelatorioEsp.gerarRelatorio&' + params + '&mime=pdf#zoom=100';
    document.querySelector('#iframeRel').src = src;
    jQuery('#iframeRel').load(function () {});
}

function fechaPDF() {
    document.querySelector("#dvPDF").classList.add("cont-inVisivel");
    document.querySelector("#opcoes_fatura").classList.remove("cont-inVisivel");
}

function preencheDadosCliente(cod) {
// projeto, classe, metodo, funcaoOK, funcaoErro, parametros
    executaServico("GSLKJava", "buscaDados", "buscaDados", function (data) {
        var jData = data;
        document.querySelector("#cclifor").value = jData.CCLIFOR;
        document.querySelector("#dtCadastro").value = jData.DTCADASTRO;
        document.querySelector("#emaill").value = jData.EMAIL;
        document.querySelector("#nome").value = jData.NOME;

        var arrNome = jData.NOME.trim().split(" ");
        var nomeUser = arrNome[0] + " " + (arrNome[arrNome.length - 1]).split("")[0];
        document.querySelectorAll("#btn_usuario_conta")[0].classList.remove("user-inVisivel");
        document.querySelectorAll("#btn_usuario_conta")[1].classList.remove("user-inVisivel");
        document.querySelectorAll("#btn_usuario_conta_nome")[0].innerHTML = '<i class="fas fa-user"></i> ' + nomeUser;
        document.querySelectorAll("#btn_usuario_conta_nome")[1].innerHTML = '<i class="fas fa-user"></i> ' + nomeUser;
        document.querySelectorAll("#header_btnEntrar")[0].classList.add("user-inVisivel");
        document.querySelectorAll("#header_btnEntrar")[1].classList.add("user-inVisivel");

        if (jData.AVANCADO) {
            document.querySelector("#endereco").value = jData.ENDERECO;
            document.querySelector("#bairro").value = jData.BAIRRO;
            document.querySelector("#cep").value = jData.CEP;
            document.querySelector("#cidade").value = jData.CIDADE;
            document.querySelector("#telefone").value = jData.FONE;
            document.querySelector("#celular").value = jData.CELULAR;
            document.querySelector("#cpf").value = jData.CPF;
            document.querySelector("#num").value = jData.NUMERO;
            if (jData.PLANO) {
                document.querySelector("#ccontrato").value = jData.CCONTRATO;
                document.querySelector("#dataContrato").value = jData.DATACONTRATO;
                document.querySelector("#valor").value = jData.VALOR;
                document.querySelector("#nomePlano").value = jData.NOMEPLANO;
                document.querySelector("#nomePagamento").value = jData.NOMEPAGAMENTO;
                document.querySelector("#qtdedias").value = jData.QTDEDIAS;
                document.querySelector("#qtdePessoas").value = jData.QTDEDIAS;
            }
        }
    }, function (erro) {
        alert("dados cli: " + erro);
    }, "&COD=" + cod);
}

function irHome(e) {
    document.querySelector(".contato").classList.remove("cont-visivel");
    document.querySelector(".contato").classList.add("cont-inVisivel");
    document.querySelector(".minhaConta").classList.remove("cont-visivel");
    document.querySelector(".minhaConta").classList.add("cont-inVisivel");
    for (var i = 0; i < document.querySelectorAll(".index").length; i++) {
        document.querySelectorAll(".index")[i].classList.remove("cont-inVisivel");
        document.querySelectorAll(".index")[i].classList.add("cont-visivel");
    }
}

function irContato(e) {
    document.querySelector(".contato").classList.remove("cont-inVisivel");
    document.querySelector(".contato").classList.add("cont-visivel");
    document.querySelector(".minhaConta").classList.remove("cont-visivel");
    document.querySelector(".minhaConta").classList.add("cont-inVisivel");
    for (var i = 0; i < document.querySelectorAll(".index").length; i++) {
        document.querySelectorAll(".index")[i].classList.add("cont-inVisivel");
        document.querySelectorAll(".index")[i].classList.remove("cont-visivel");
    }
}

function irConta(e) {
    document.querySelectorAll(".index")[0].classList.remove("cont-visivel");
    document.querySelectorAll(".index")[0].classList.add("cont-inVisivel");
    document.querySelectorAll(".index")[1].classList.remove("cont-visivel");
    document.querySelectorAll(".index")[1].classList.add("cont-inVisivel");
    document.querySelector(".contato").classList.remove("cont-visivel");
    document.querySelector(".contato").classList.add("cont-inVisivel");
    document.querySelector(".minhaConta").classList.add("cont-visivel");
    document.querySelector(".minhaConta").classList.remove("cont-inVisivel");
}

function getTela(e) {
    document.querySelector(".divCadastroTransition").style.left = "0";
    document.querySelector('.visivel').classList.remove('visivel');
    document.querySelector('.selected').classList.remove('selected');
    e.target.classList.add('selected');
    document.querySelector('.' + e.target.dataset.settela).classList.add('visivel');
}

function fecharTela(e) {
    document.querySelector('.visivell').classList.remove('visivell');
    document.querySelector('.tampaBackBody').classList.remove('tampaBackBody');
}

function abrirTela(e) {
    document.querySelector('#tbody').classList.add('tampaBackBody');
    document.querySelector('#divEntrar').classList.add('visivell');
    document.querySelector('.entrarStyle').style.minHeight = "63%";
}


// SCRIPT-ESTILIZAÇÃO //
var slideIndex = 1;
showSlides(slideIndex);
function plusSlides(n) {
    showSlides(slideIndex += n);
}

function currentSlide(n) {
    showSlides(slideIndex = n);
}

function showSlides(n) {
    var i;
    var slides = document.getElementsByClassName("mySlides");
    var dots = document.getElementsByClassName("dot");
    if (n > slides.length) {
        slideIndex = 1;
    }
    if (n < 1) {
        slideIndex = slides.length;
    }
    for (i = 0; i < slides.length; i++) {
        slides[i].style.display = "none";
    }
    for (i = 0; i < dots.length; i++) {
        dots[i].className = dots[i].className.replace(" active", "");
    }
    slides[slideIndex - 1].style.display = "block";
    dots[slideIndex - 1].className += " active";
}

function fechaTelaSelectPlano() {
    document.querySelector(".tampaBackPromocao").classList.add("cont-inVisivel");
}

init();