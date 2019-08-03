package br.com.tecnicon.controller;

import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ChatBox", urlPatterns =
{
    "/ChatBox"
})
public class ChatBox extends HttpServlet
{

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws br.com.tecnicon.server.execoes.ExcecaoTecnicon
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ExcecaoTecnicon
    {
        response.setContentType("text/html;charset=UTF-8");

        VariavelSessao vs = new TVariavelSessao();
        vs.addParametros("sessao", "-9876");
        request.getSession().setAttribute("vs", vs);
//        Várias sessões no portal, comentado pra diminuir o consumo de memório
//        RepositorioSession.getInstance().setAttribute(request.getSession().getId(), vs);

        try (PrintWriter out = response.getWriter())
        {
            Map<String, String[]> mp = vs.getParameterMap();
            mp.put("cobjetohtml", new String[]
            {
                "3782"
            });
            vs.setParameterMap(mp);
            Object obj = TecniconLookup.lookup("TecniconEspecialHTML", "ObjetosHTML");
            out.println(((String) obj.getClass().getMethod("retornaObjetoHTML", VariavelSessao.class).invoke(obj, vs)));
        } catch (Exception ex)
        {
            throw new ExcecaoTecnicon(vs,ex);
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
