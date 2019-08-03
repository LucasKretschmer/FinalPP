package br.com.tecnicon.controller;

import br.com.tecnicon.criptografia.PWSec;
import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ivandro.lessing
 */
@WebServlet(name = "EmailAtividade", urlPatterns =
{
    "/EmailAtividade"
})
public class EmailAtividade extends HttpServlet
{

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        VariavelSessao vs = new TVariavelSessao();
        try
        {
            response.setCharacterEncoding("UTF8");

            String tpAviso = request.getParameter("t");
            String parametros = request.getParameter("p");
            System.out.println("parametros n:" + parametros);

            parametros = PWSec.decrypt(parametros);

            System.out.println("parametros:" + parametros);
            String[] arrPar = parametros.split(",");
            String seq = arrPar[0];
            String empresa = arrPar[1];

            vs.addParametros("empresa", "" + empresa);
            vs.addParametros("senha", "");
            vs.addParametros("nome", "SYSDBA");
            vs.addParametros("usuario", "SYSDBA");
            vs.addParametros("sessao", "-9876");

            if (tpAviso.equals("avEx"))/*RECEBEU EMAIL EXTERNO*/

            {
                TSQLDataSetEmp update = TSQLDataSetEmp.create(vs);

                update.execSQL("UPDATE ATIVIDADERECEXTERNO SET DTREC = TCURRENT_DATE(), HRREC = TCURRENT_TIME() WHERE DTREC IS NULL AND ATIVIDADERECEXTERNO.SATIVIDADERECEXTERNO=" + seq);

                StringBuffer conteudo = new StringBuffer();
                conteudo.append("<!doctype html>");
                conteudo.append("<html>");
                conteudo.append("<head>");
                conteudo.append("<meta charset=\"utf-8\">");
                conteudo.append("<title>TECNICON Sistemas Gerenciais</title>");
                conteudo.append("</head>");
                conteudo.append("");
                conteudo.append("<body>");
                conteudo.append("");

                conteudo.append("DATE:").append(new Date());
                

                conteudo.append("");
                conteudo.append("</body>");
                conteudo.append("</html>");
                PrintWriter out = response.getWriter();
                out.println(conteudo.toString());
                out.close();

            } else if (tpAviso.equals("avIn"))/*RECEBEU EMAIL EXTERNO*/

            {
                TSQLDataSetEmp update = TSQLDataSetEmp.create(vs);

                update.execSQL("UPDATE ATIVIDADECONVIDADO SET RECEBIDO = 'S' WHERE RECEBIDO IS NULL OR RECEBIDO = 'N' AND ATIVIDADECONVIDADO.SATIVIDADECONVIDADO=" + seq);

                StringBuffer conteudo = new StringBuffer();
                conteudo.append("<!doctype html>");
                conteudo.append("<html>");
                conteudo.append("<head>");
                conteudo.append("<meta charset=\"utf-8\">");
                conteudo.append("<title>TECNICON Sistemas Gerenciais</title>");
                conteudo.append("</head>");
                conteudo.append("");
                conteudo.append("<body>");
                conteudo.append("");

                conteudo.append("DATE:").append(new Date());

                conteudo.append("");
                conteudo.append("</body>");
                conteudo.append("</html>");
                PrintWriter out = response.getWriter();
                out.println(conteudo.toString());
                out.close();

            } else if (tpAviso.equals("av"))/*RECEBEU*/

            {
                TSQLDataSetEmp update = TSQLDataSetEmp.create(vs);

                update.execSQL("UPDATE ATIVIDADEEMAIL SET DTREC = TCURRENT_DATE(), HRREC = TCURRENT_TIME() WHERE DTREC IS NULL AND ATIVIDADEEMAIL.SATIVIDADEEMAIL=" + seq);

                StringBuffer conteudo = new StringBuffer();
                conteudo.append("<!DOCTYPE html>");
                conteudo.append("<html lang=\"pt-br\">");
                conteudo.append("<head>");
                conteudo.append("<meta charset=\"utf-8\">");
                conteudo.append("<title>TECNICON Sistemas Gerenciais</title>");
                conteudo.append("</head>");
                conteudo.append("");
                conteudo.append("<body>");

                conteudo.append("DATE:").append(new Date());

                conteudo.append("</body>");
                conteudo.append("</html>");
                PrintWriter out = response.getWriter();
                out.println(conteudo.toString());
                out.close();
            } else if (tpAviso.equals("co"))/*CONFIRMACAO*/

            {
                TSQLDataSetEmp update = TSQLDataSetEmp.create(vs);
                TSQLDataSetEmp verifica = TSQLDataSetEmp.create(vs);
                verifica.commandText("SELECT ATIVIDADEEMAIL.SATIVIDADEEMAIL FROM ATIVIDADEEMAIL WHERE ATIVIDADEEMAIL.SATIVIDADEEMAIL = " + seq + " AND DTCONF IS NULL");
                verifica.open();

                StringBuffer conteudo = new StringBuffer();
                if (!verifica.isEmpty())
                {

                    update.execSQL("UPDATE ATIVIDADEEMAIL SET DTCONF = TCURRENT_DATE(), HRCONF = TCURRENT_TIME() WHERE DTCONF IS NULL AND ATIVIDADEEMAIL.SATIVIDADEEMAIL=" + seq);

                    conteudo.append("<!DOCTYPE html>\n"
                            + "<html lang=\"pt-br\">\n"
                            + "<head>\n"
                            + "	<meta charset=\"UTF-8\">\n"
                            + "	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
                            + "	<title>Confirmação de E-mail</title>\n"
                            + "	<link rel=\"stylesheet\" href=\"style/atividadeEmail.css\">\n"
                            + "</head>\n"
                            + "<body>\n"
                            + "	<div class=\"container\">\n"
                            + "		<div class=\"content-confirm-email\">\n"
                            + "			<img src=\"images/logo-tecnicon-white.png\" alt=\"Logotipo da TECNICON\" />\n"
                            + "			<p><span class=\"txt-confirm\">Seu e-mail foi confirmado com sucesso!</span></p>\n"
                            + "			<br />\n"
                            + "			<img src=\"images/check-email.png\" alt=\"E-mail checado e aprovado\">\n"
                            + "			<p><span class=\"txt-copyright\">Conheça nosso site: <a href=\"http://www.tecnicon.com.br\">tecnicon.com.br</a> <br />\n"
                            + "				© TECNICON Sistemas Gerenciais</span></p>\n"
                            + "		</div>\n"
                            + "	</div>\n"
                            + "</body>\n"
                            + "</html>");
                } else
                {
                    conteudo.append("<!DOCTYPE html>\n"
                            + "<html lang=\"pt-br\">\n"
                            + "<head>\n"
                            + "	<meta charset=\"UTF-8\">\n"
                            + "	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
                            + "	<title>Confirmação de E-mail</title>\n"
                            + "	<link rel=\"stylesheet\" href=\"style/atividadeEmail.css\">\n"
                            + "</head>\n"
                            + "<body>\n"
                            + "	<div class=\"container\">\n"
                            + "		<div class=\"content-confirm-email\">\n"
                            + "			<img src=\"images/logo-tecnicon-white.png\" alt=\"Logotipo da TECNICON\" />\n"
                            + "			<p><span class=\"txt-confirm\">Seu e-mail já foi confirmado!</span></p>\n"
                            + "			<br />\n"
                            + "			<img src=\"images/check-email.png\" alt=\"E-mail checado e aprovado\">\n"
                            + "			<p><span class=\"txt-copyright\">Conheça nosso site: <a href=\"http://www.tecnicon.com.br\">tecnicon.com.br</a> <br />\n"
                            + "				© TECNICON Sistemas Gerenciais</span></p>\n"
                            + "		</div>\n"
                            + "	</div>\n"
                            + "</body>\n"
                            + "</html>");
                }
                PrintWriter out = response.getWriter();
                out.println(conteudo);
                out.close();
            }

        } catch (Exception ex)
        {
            new ExcecaoTecnicon(vs, ex);
        } finally
        {
        }

    }

    @Override
    public String getServletInfo()
    {
        return "Short description";
    }
}
