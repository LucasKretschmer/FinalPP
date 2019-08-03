package br.com.tecnicon.controller;

import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jean.siqueira
 */
@WebServlet(name = "Ead", urlPatterns =
{
    "/Ead"
})
public class Ead extends HttpServlet
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
        try
        {
            if (request.getParameter("codclisite") != null || request.getParameter("SMENU") != null)
            {
                VariavelSessao vs = null;
                if (request.getParameter("codclisite") != null)
                {
                    vs = new TVariavelSessao();
                    vs.addParametros("sessao", "-9876");
                    vs.addParametros("codclisite", request.getParameter("codclisite"));
                    vs.addParametros("cusuariocli", request.getParameter("cusuariocli"));
                    vs.addParametros("nomecompletocli", request.getParameter("nomecompletocli"));
                    vs.addParametros("usuariocli", request.getParameter("usuariocli"));
                    vs.addParametros("emailcli", request.getParameter("emailcli"));
                    vs.addParametros("filialExecutaPortal", request.getParameter("filialExecutaPortal") != null ? request.getParameter("filialExecutaPortal") : "");
                } else if (request.getParameter("cuser") != null)
                {
                    vs = new TVariavelSessao();
                    vs.addParametros("sessao", "-9876");
                    vs.addParametros("cuser", request.getParameter("cuser"));
                    vs.addParametros("empresa", request.getParameter("empresa"));
                    vs.addParametros("cformulario", request.getParameter("cformulario"));
                }

                if (vs != null)
                {
                    if (request.getParameter("SMENU") != null)
                    {
                        vs.addParametros("smenu", request.getParameter("SMENU"));
                    }

                    vs.addParametros("tipo", request.getParameter("tipo"));
                    vs.addParametros("codigo", request.getParameter("codigo"));
                    final VariavelSessao vs2 = vs.clone();

                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Object obj = TecniconLookup.lookup("SisHelp/AcessoEAD");
                                obj.getClass().getMethod("registrarAcesso", VariavelSessao.class).invoke(obj, vs2);
                            } catch (ExcecaoTecnicon | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                            {
                            }
                        }
                    }).start();
                }

            }

        } catch (Exception ex)
        {
            //Just keep walking
        }

        RequestDispatcher r = request.getRequestDispatcher("Controller");
        r.forward(request, response);
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
        return "Servlet responsável por tratar as requsições do EAD";
    }
}
