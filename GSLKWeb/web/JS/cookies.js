function logar(cgc, cuser, nuser, email) {
    setCookie("cod", cuser, 3600);
    setCookie("user", nuser, 3600);
    setCookie("cgc", cgc, 3600);
    setCookie("email", email, 3600);
}

function atualizarCookie() {
    var arrValues = ["cod", "cgc", "user", "email"];
    for (var i = 0; i < 3; i++) {
        var valorCookie = getCookie(arrValues[i]);
        setCookie(arrValues[i], valorCookie, 3600);
    }
}
function setCookie(nome, value, expira) {
    document.cookie = encodeURIComponent(nome) + "=" + encodeURIComponent(value) + "; expires=" + expira + "; path=";
}

function deslogar() {
    removeCookie("cod");
    removeCookie("user");
    removeCookie("email");
    removeCookie("cgc");
}

function removeCookie(name) {
    setCookie(name, '', 0);
}

function verificaLogado() {
    var arrValues = ["cod", "user", "email", "cgc"];
    var valuesReq = [];
    for (var x = 0; x < 3; x++) {
        var valorCookie = getCookie(arrValues[x]);
        if (valorCookie !== false) {
            valuesReq[x] = valorCookie;
        } else {
            return false;
        }
    }
    return true;
}

function getCookie(nome) {
    var cookie = document.cookie.split(";");
    for (var i = 0; i < cookie.length; i++) {
        if (cookie[i].trim().split("=")[0] === nome) {
            return cookie[i].trim().split("=")[1];
        }
    }
}
