/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.paginas;

import br.com.tecnicon.server.Propriedades;
import br.com.tecnicon.server.RetornaCSSJS;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jean.siqueira
 */
public class Index
{

    public static String retornaIndex(String dispositivo, String navegadordelphi, String tipoAcesso, String navegadorswing, String tipologin, HttpServletRequest request) throws ExcecaoTecnicon
    {
        return retornaIndex(dispositivo, navegadordelphi, tipoAcesso, navegadorswing, tipologin, request, "");
    }

    public static String retornaIndex(String dispositivo, String navegadordelphi, String tipoAcesso, String navegadorswing, String tipologin, HttpServletRequest request, String abrechat) throws ExcecaoTecnicon
    {
        //Propriedades.setServidorTecnicon(true);
        StringBuilder sb = new StringBuilder();
        
        //Pra abrir chat automatico pelas notificações
        String chats = "";
        if(abrechat != null && !abrechat.equals("")){
            chats = "abreChat='" + abrechat + "'";
        }
        /////////////////////////////////////////////

        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("    <head>");
        sb.append("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        sb.append("        <META HTTP-EQUIV=\"CACHE-CONTROL\" CONTENT=\"NO-CACHE\">");
        sb.append("        <META HTTP-EQUIV=\"PRAGMA\" CONTENT=\"NO-CACHE\">");
        sb.append("         <meta name=\"theme-color\" content=\"#064A77\">");
        //sb.append("        <!-- COMPATIBILIDADE PARA IE -->");
        sb.append("        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">");
        //sb.append("        <!-- Mobile -->");
        //sb.append("        <meta name=\"viewport\" content='minimum-scale=1.0, width=device-width, maximum-scale=1.0, user-scalable=no, initial-scale=1' />");
        sb.append("        <meta name=\"viewport\" content='minimum-scale=1.0, width=device-width, initial-scale=1' />");
//        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        sb.append("        <meta name=\"SKYPE_TOOLBAR\" content=\"SKYPE_TOOLBAR_PARSER_COMPATIBLE\" />");
        sb.append("        <title>TECNICON Business Suite</title>");
        String navegador = (String) request.getSession(true).getAttribute("navegador");
        if (navegador == null)
        {
            navegador = "chrome";
        }

        String versao = (String) request.getSession(true).getAttribute("versao");
        if (versao == null)
        {
            versao = "54";
        }
        sb.append(RetornaCSSJS.retornarCSSJS(dispositivo, Propriedades.isServidorTecnicon(), navegadordelphi.equals("true"), navegador, versao, navegadorswing.equalsIgnoreCase("true")));
        sb.append("    </head>");
        sb.append("    <body style='background: #064A77;' " + chats + " onload=\"navegadorswing=").append(navegadorswing).append(";mudarTitulo=" + navegadordelphi + ";tipologin='" + tipologin + "';telaInicial(" + (tipoAcesso != null ? "'" + tipoAcesso + "'" : "'objeto'") + ");\">");
        sb.append("    </body>");
        // sb.append("   <script type=\"text/javascript\">var xStart,yStart=0;document.addEventListener('touchstart',function(e){xStart=e.touches[0].screenX;yStart=e.touches[0].screenY;});document.addEventListener('touchmove',function(e){var xMovement=Math.abs(e.touches[0].screenX-xStart);var yMovement=Math.abs(e.touches[0].screenY-yStart);if((yMovement*3)>xMovement){e.preventDefault();}});</script>");
        sb.append("</html>");
        sb.append("");

        return sb.toString();

    }

    public static String retornaPrincipal(String dispositivo, String navegadordelphi, VariavelSessao vs, String navegadorswing, HttpServletRequest request)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>");
            sb.append("<html>");
            sb.append("    <head>");
            sb.append("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
            //sb.append("        <!-- COMPATIBILIDADE PARA IE -->");
            sb.append("        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">");
            //sb.append("        <!-- Mobile -->");
            //sb.append("        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />");
            sb.append("         <meta name=\"theme-color\" content=\"#064A77\">");
            sb.append("         <meta name=\"SKYPE_TOOLBAR\" content=\"SKYPE_TOOLBAR_PARSER_COMPATIBLE\" />");
            sb.append("        <title>TECNICON Business Suite</title>");
            sb.append(RetornaCSSJS.retornarCSSJS(dispositivo, Propriedades.isServidorTecnicon(), navegadordelphi.equals("true"), (String) request.getSession(true).getAttribute("navegador"), (String) request.getSession(true).getAttribute("versao")));
            sb.append("    </head>");
            //sb.append("    <!--oncontextmenu=\"return false;\"tAjax('Tecnicon.Login&bloqueado=F','erptecnicon','=','Login',{minimiza:false ,maximiza:false, fecha:false}) -->");
            //quemsou,sessao,usuario,nome, email,versao,versaobanco,empresa,filial,local,nomeempresa,nomefilial,nomelocal

            sb.append("    <body style='background: #064A77;' onload=\"navegadorswing=").append(navegadorswing).append(";mudarTitulo=").append(navegadordelphi).
                    append(";atualizarPrincipal(jQuery('#div-desktop'),'").
                    append(vs.getValor("sessao")).append("','").
                    append(vs.getValor("cusuario")).append("','").
                    append(vs.getValor("nome")).append("','").
                    append(vs.getValor("email")).append("','").
                    append(vs.getValor("versaosis")).append("','").
                    append(vs.getValor("versaobanco")).append("','").
                    append(vs.getValor("empresa")).append("','").
                    append(vs.getValor("filial")).append("','").
                    append(vs.getValor("local")).append("','").
                    append(vs.getValor("nempresa")).append("','").
                    append(vs.getValor("nfilial")).append("','").
                    append(vs.getValor("nlocal")).append("');fecharDialog('locais');tAjax('Tecnicon.Principal','div-desktop','inner');\">");
            sb.append("        <div id=\"erptecnicon\"></div>");
            sb.append("        <div id=\"div-desktop\"></div>");
            sb.append("    </body>");
            sb.append("</html>");
            return sb.toString();
        } catch (ExcecaoTecnicon ex)
        {
            return "erro:" + ex.getMessage();
        }
    }
}
