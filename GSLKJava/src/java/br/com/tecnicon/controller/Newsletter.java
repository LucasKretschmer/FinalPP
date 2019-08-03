/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.controller;

import br.com.tecnicon.server.context.TClassLoader;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jean.siqueira
 */
@WebServlet(name = "Newsletter", urlPatterns =
{
    "/Newsletter"
})
public class Newsletter extends HttpServlet
{

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
        doPost(request, response);
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

        VariavelSessao vs = new TVariavelSessao();
        vs.addParametros("t", request.getParameter("t"));
        vs.addParametros("p", request.getParameter("p"));

        try
        {
            String conteudo = (String) TClassLoader.execMethod("TecniconEnvioDeEmail/InteracaoNews", "registraInteracao", vs);
            PrintWriter out = response.getWriter();
            out.println(conteudo);
            out.close();
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
