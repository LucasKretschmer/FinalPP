package br.com.tecnicon.controller;

import br.com.tecnicon.server.RetornaCSSJS;
import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ilario.david
 */
@WebServlet(name = "Survey", urlPatterns =
{
    "/Survey"
})
public class Survey extends HttpServlet
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

        VariavelSessao vs = new TVariavelSessao();

        response.setContentType("text/html;charset=UTF-8");

        if (request.getParameter("acao") == null)
        {
            if (request.getParameter("survey") != null)
            {
                String url;
                try
                {
                    url = new String(Base64.getDecoder().decode(request.getParameter("survey")));
                } catch (Exception ex)
                {
                    url = request.getParameter("survey");
                }

                for (String par : url.split("&"))
                {
                    vs.addParametros(par.split("=")[0], par.split("=")[1]);
                }

                vs.addParametros("sessao", "-9876");
                
                //request.getSession().setAttribute("vs", vs);
                //RepositorioSession.getInstance().setAttribute(request.getSession().getId(), vs);
            }

        } else
        {
            RequestDispatcher r = request.getRequestDispatcher("Controller");
            r.forward(request, response);
        }

        try (PrintWriter out = response.getWriter())
        {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("   <head>");
            out.println("       <title>Tecnicon Survey</title>");

            StringBuilder sb = new StringBuilder();
            RetornaCSSJS.retonaCSSGenerico(false, false, vs, sb);

            out.println(sb.toString());

            out.println("   </head>");
            out.println("<body>");

            try
            {
                Object loaderIndex = TecniconLookup.lookup("TecniconSurvey", "LoaderIndex");
                out.println(
                        loaderIndex.getClass().getMethod("loadHTML", VariavelSessao.class).invoke(loaderIndex, vs)
                );

            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException ex)
            {
                out.println(ex.getMessage());
            }

            out.println("</body>");
            out.println("</html>");
        } catch (ExcecaoTecnicon ex)
        {

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
