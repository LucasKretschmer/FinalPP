package br.com.tecnicon.controller;

import br.com.tecnicon.objects.put.DadosEmpresa;
import br.com.tecnicon.server.Propriedades;
import br.com.tecnicon.server.RetornaCSSJS;
import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.dataset.TSQLDataSetEmp;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "LMS", urlPatterns =
{
    "/LMS/*", "/lms/*"
})
public class LMS extends HttpServlet
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
            throws ServletException, IOException, ExcecaoTecnicon
    {
        if (Propriedades.isServidorCloud())
        {
            response.sendRedirect("https://portal.tecnicon.com.br:62151/Tecnicon/LMS");
        }
        
        response.setContentType("text/html;charset=UTF-8");

        if (request.getParameter("acao") == null)
        {

            VariavelSessao vs = new TVariavelSessao();
            vs.addParametros("sessao", "-9876");
            vs.addParametros("request", request);
            vs.addParametros("response", response);

            /* recuperacao de senha */
            if (request.getParameter("rpasswd") != null)
            {
                String url = new String(Base64.getDecoder().decode(request.getParameter("rpasswd")));
                if (url.matches("al=[0-9]{1,10}&ce=[0-9]{1,10}&f=[0-9]{1,10}&dtl=[0-9]{1,50}"))
                {

                    String[] parametro = url.split("&");

                    Map<String, String> hash = new HashMap<>();

                    for (String par : parametro)
                    {
                        hash.put(par.split("=")[0], par.split("=")[1]);
                    }

                    vs.addParametros("empresa", hash.get("ce"));
                    vs.addParametros("filial", hash.get("f"));
                    vs.addParametros("clmsaluno", hash.get("al"));
                    vs.addParametros("expiraem", hash.get("dtl"));
                } else
                {
                    throw new ExcecaoTecnicon(vs, " URL PARA REDEFINIR SENHA NÃO BATE COM O PADRÃO ESPERADO! ");
                }

            } else
            {
                TClientDataSet empresa = DadosEmpresa.getEmpresas("", "CEMPRESA");
                while (!empresa.eof())
                {
                    vs.addParametros("empresa", empresa.fieldByName("CEMPRESA").asString());

                    TSQLDataSetEmp filial = TSQLDataSetEmp.create(vs);
                    filial.commandText("SELECT FIRST 1 CFILIAL FROM FILIAL");
                    filial.open();

                    vs.addParametros("filial", filial.fieldByName("CFILIAL").asString());

                    filial.close();
                    filial.commandText("SELECT FIRST 1 CLMSCURSO FROM LMSCURSO");
                    filial.open();

                    if (!filial.isEmpty())
                    {
                        break;
                    }

                    empresa.next();
                }
            }

            //request.getSession().setAttribute("vs", vs);
            //RepositorioSession.getInstance().setAttribute(request.getSession().getId(), vs);

            try (PrintWriter out = response.getWriter())
            {
                out.println("<!DOCTYPE html>");
                out.println("<html lang='pt-BR'>");
                out.println("<head> ");
                out.println("<title>LMS - Solução em Ensino a Distância Tecnicon</title>");
                out.println("<meta name='description' content='LMS plataforma online especializada em oferecer ao usuário capacitação e gerenciamento total dos cursos à distância de forma prática e eficiente'>");
                out.println("<meta name='keywords' content='LMS, Aprendizam, Conhecimento, Curso, ERP, Plataforma, Gestão, Ensino'>");
                out.println("<meta name='robots' content='index, follow'>");
                out.println("<meta name='revisit-after' content='1 day'>");
                out.println("<meta name='language' content='Portuguese'>");
                out.println("<meta name='generator' content='N/A'>");
                out.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
                out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                out.println("<meta property='og:title' content='LMS - Solução em Ensino a Distância Tecnicon'>");
                out.println("<meta property='og:description' content='LMS plataforma online especializada em oferecer ao usuário capacitação e gerenciamento total dos cursos à distância de forma prática e eficiente'>");
                out.println("<meta property='og:type' content='website'>");
                out.println("<meta property='og:site_name' content='LMS - TECNICON'>");
                out.println("<meta property='og:image' content='https://portal.tecnicon.com.br:62151/Tecnicon/RetornaImg?tipo=BLOG&nomeArquivo=-1716556939_267272_lms.png'>");
                StringBuilder sb = new StringBuilder();
                RetornaCSSJS.retonaCSSGenerico(false, true, vs, sb);
                out.println(sb.toString());

                out.println("</head>");
                out.println("<body>");

                if (request.getParameter("clmscurso") != null)
                {
                    String curso = request.getParameter("clmscurso");
                    vs.addParametros("carregar", curso);
                }

                String retorno;
                try
                {
                    if (request.getParameter("clmscurso") != null)
                    {
                        vs.addParametros("CLMSCURSO", request.getParameter("clmscurso"));
                    }

                    Object objeto = TecniconLookup.lookup("LMSCursoNew", "LMSHomeNew");
                    retorno = (String) objeto.getClass().getMethod("carregaHome", VariavelSessao.class).invoke(objeto, vs);
                    out.println(retorno);
                } catch (Exception ex)
                {

                    out.println(ExcecaoTecnicon.getCauseMessage(ex).getMessage());

                    StringWriter stringWriteStackTrace = new StringWriter();
                    PrintWriter pw = new PrintWriter(stringWriteStackTrace);
                    //ex.printStackTrace(pw);
                    out.println(stringWriteStackTrace.toString().replaceAll("\r\n", "<br />").replaceAll("	", "&nbsp;&nbsp;&nbsp;&nbsp;"));
                }

                out.println("</body>");
                out.println("</html>");
            }
        } else
        {
            RequestDispatcher r = request.getRequestDispatcher("Controller");
            r.forward(request, response);
        }
    }

    public String carregaHTMLJS(VariavelSessao vs) throws ExcecaoTecnicon
    {
        try
        {
            Object obj = TecniconLookup.lookup("TecniconTBPDS", "Control");
            String comps = (String) obj.getClass().getMethod("carregaWebComponents", VariavelSessao.class).invoke(obj, vs);

            TClientDataSet JAVASCRIPT = TClientDataSet.create(vs, "JAVASCRIPT");
            JAVASCRIPT.createDataSet();
            JAVASCRIPT.condicao("WHERE JAVASCRIPT.CJAVASCRIPT = 2888"//JSIncluirIndex
                    + "     OR JAVASCRIPT.CJAVASCRIPT = 2428"//TecniconTDataSet
                    + "     OR JAVASCRIPT.CJAVASCRIPT = 2796"//JSAdicionais
                    + "     OR JAVASCRIPT.CJAVASCRIPT = 2438"//Tecnicon3
                    + "     OR JAVASCRIPT.CJAVASCRIPT = 4388"//Falador
                    + "     OR JAVASCRIPT.CJAVASCRIPT = 5682"//AssistentDifVusual
            );
            JAVASCRIPT.ordenar("ORDER BY JAVASCRIPT.CJAVASCRIPT");
            JAVASCRIPT.open();

            String jss = "";
            while (!JAVASCRIPT.eof())
            {
                jss += JAVASCRIPT.fieldByName("CODIGOJS").asString();
                JAVASCRIPT.next();
            }

            return comps + "<script>" + jss + "</script>";
        } catch (Exception ex)
        {
            return "";
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
        try
        {
            processRequest(request, response);
        } catch (ExcecaoTecnicon ex)
        {
        }
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
        try
        {
            processRequest(request, response);
        } catch (ExcecaoTecnicon ex)
        {
        }
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
