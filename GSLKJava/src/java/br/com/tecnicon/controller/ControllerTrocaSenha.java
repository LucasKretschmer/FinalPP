package br.com.tecnicon.controller;

import br.com.tecnicon.criptografia.PWSec;
import br.com.tecnicon.criptografia.SHA256;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Jacob
 */
@WebServlet(name = "TROCASENHA", urlPatterns =
{
    "/TROCASENHA"
})
public class ControllerTrocaSenha extends HttpServlet
{

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try
        {
            TVariavelSessao vs = new TVariavelSessao();
            vs.addParametros("sessao", "-9876");

            Map<String, String[]> mp = vs.getParameterMap();

            String conteudo = "";
            String senha = "", cusuario = "", cempresa = "", link1 = "", valSenha = "";

            try
            {
                //Começa no indice 1 pois recebe primeiro um "&"
                String tmp[] = PWSec.decrypt(request.getParameter("s")).split("&");
                senha = tmp[1].split("=")[1];
                cusuario = tmp[2].split("=")[1];
                cempresa = tmp[3].split("=")[1];
                link1 = tmp[4].split("=")[1];
                valSenha = tmp[5].split("=")[1];

            } catch (BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException ex)
            {
                return;
            }

            if (valSenha != null && !valSenha.equals(""))
            {
                if (!valSenha.equals(Funcoes.dateToStr(new Date())))
                {
                    conteudo = "<table style=\"width:100%;line-height: 40px;\">"
                            + "  <tr>"
                            + "    <td colspan=\"2\" align=\"center\" style=\"font-size: 40px;\">"
                            + "      TECNICON Business Suite - Recuperação de Senha"
                            + "    </td>"
                            + "  </tr>"
                            + "  <tr>"
                            + "    <td colspan=\"1\" align=\"center\">"
                            + "      <img src=\"http://tecnicon.com.br/images/logo_tecnicon_email.png\""
                            + "    </td>"
                            + "  </tr>"
                            + "  <tr>"
                            + "    <td colspan=\"2\" align=\"center\" style=\"font-size: 18px;\">"
                            + "      Esta senha expirou!"
                            + "    </td>"
                            + "  </tr>"
                            + "</table>";

                    out.println(conteudo);
                    return;
                }
            }

            vs.setParameterMap(mp);
            if (cempresa != null && !cempresa.equals("") && !cempresa.equals("N"))
            {
                vs.addParametros("empresa", cempresa);
            }

            if (cusuario == null || "".equals(cusuario))
            {
                conteudo = "<table style=\"width:100%;line-height: 40px;\">"
                        + "  <tr>"
                        + "    <td colspan=\"2\" align=\"center\" style=\"font-size: 40px;\">"
                        + "      TECNICON Business Suite - Recuperação de Senha"
                        + "    </td>"
                        + "  </tr>"
                        + "  <tr>"
                        + "    <td colspan=\"1\" align=\"center\">"
                        + "      <img src=\"http://tecnicon.com.br/images/logo_tecnicon_email.png\""
                        + "    </td>"
                        + "  </tr>"
                        + "  <tr>"
                        + "    <td colspan=\"2\" align=\"center\" style=\"font-size: 18px;\">"
                        + "      Não foi possível realizar a alteração da senha!"
                        + "    </td>"
                        + "  </tr>"
                        + "</table>";

                out.println(conteudo);
                return;
            }

            TClientDataSet cds = TClientDataSet.create(vs, "USUARIO");
            cds.createDataSet();
            cds.condicao("WHERE USUARIO.CUSUARIO = " + cusuario);
            cds.open();

            if (cds.isEmpty())
            {
                conteudo = "<table style=\"width:100%;line-height: 40px;\">"
                        + "  <tr>"
                        + "    <td colspan=\"2\" align=\"center\" style=\"font-size: 40px;\">"
                        + "      TECNICON Business Suite - Recuperação de Senha"
                        + "    </td>"
                        + "  </tr>"
                        + "  <tr>"
                        + "    <td colspan=\"1\" align=\"center\">"
                        + "      <img src=\"http://tecnicon.com.br/images/logo_tecnicon_email.png\""
                        + "    </td>"
                        + "  </tr>"
                        + "  <tr>"
                        + "    <td colspan=\"2\" align=\"center\" style=\"font-size: 18px;\">"
                        + "      Não foi possível realizar a alteração da senha!"
                        + "    </td>"
                        + "  </tr>"
                        + "</table>";

                out.println(conteudo);
                return;
            }

            String novaSenha = senha;
            try
            {
                novaSenha = SHA256.encodePswd(vs, novaSenha);
            } catch (NoSuchAlgorithmException ex)
            {
                throw new ExcecaoTecnicon(vs, ex);
            }
            cds.edit();
            cds.fieldByName("SENHA").asString(novaSenha);
            cds.fieldByName("SENHADTALT").asDateTime(new Date());
            cds.post();

            conteudo = "<table style=\"width:100%;line-height: 40px;\">"
                    + "  <tr>"
                    + "    <td colspan=\"2\" align=\"center\" style=\"font-size: 40px;\">"
                    + "      TECNICON Business Suite - Recuperação de Senha"
                    + "    </td>"
                    + "  </tr>"
                    + "  <tr>"
                    + "    <td colspan=\"1\" align=\"center\">"
                    + "      <img src=\"http://tecnicon.com.br/images/logo_tecnicon_email.png\""
                    + "    </td>"
                    + "  </tr>"
                    + "  <tr>"
                    + "    <td colspan=\"2\" align=\"center\" style=\"font-size: 18px;\">"
                    + "      Senha alterada com sucesso!"
                    + "    </td>"
                    + "  </tr>"
                    + "  <tr>"
                    + "    <td colspan=\"2\" align=\"center\" style=\"color:#0096db; font-size: 18px;\">"
                    + "      Nova senha"
                    + "    </td>"
                    + "  </tr>"
                    + "  <tr>"
                    + "    <td colspan=\"2\" align=\"center\" style=\"font-size: 40px; line-height: 80px;\" ><label style=\" padding: 20px; border: 1px solid #0096db; border-radius: 10px; \">"
                    + senha
                    + "    </label></td>"
                    + "  </tr>"
                    + "  <tr>"
                    + "    <td colspan=\"2\" align=\"center\" style=\"font-size: 18px;\">"
                    + "      <a>Acesse o sistema utilizando sua nova senha<br><a href=\"" + link1 + "\" target=\"_blank\"> " + link1 + "</a>"
                    + "    </td>"
                    + "  </tr>"
                    + "</table>";

            out.println(conteudo);

        } catch (Exception ex)
        {
            out.println("erro:" + ex.getMessage());
        }
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

}
