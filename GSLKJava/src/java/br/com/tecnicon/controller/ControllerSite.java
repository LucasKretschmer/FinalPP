/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.controller;

import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author eduardo.wohlfahrt
 */
@WebServlet(name = "Site", urlPatterns =
{
    "/Site"
})
public class ControllerSite extends HttpServlet
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
        
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.addHeader("Access-Control-Max-Age", "86400");
        response.setContentType("text/html;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try
        {
            String sessao = request.getParameter("sessao");
            if(sessao == null || sessao.equals(""))
                throw new ExcecaoTecnicon(null,"Sessão Inválida!");
            
            String empresa = request.getParameter("empresa");
            if(empresa == null || empresa.equals(""))
                throw new ExcecaoTecnicon(null,"Empresa Inválida");
            
            String filial = request.getParameter("filial");
            if(filial == null || filial.equals(""))
                throw new ExcecaoTecnicon(null,"Filial Inválida");
            
            String filialcf = request.getParameter("filialcf");
            if(filialcf == null || filialcf.equals(""))
                throw new ExcecaoTecnicon(null,"FilialCF Inválida");
            
            TVariavelSessao vs = new TVariavelSessao();
            vs.addParametros("sessao", sessao);
            vs.addParametros("empresa", empresa);
            vs.addParametros("filial", filial);
            vs.addParametros("filialcf", filialcf);
            
            String parametros = request.getParameter("parametros");
            
            if(parametros != null && !parametros.equals("")) 
            {
                String param[] = parametros.split("}}");
                for(String p : param) 
                {
                    String attr[] = p.split("=");
                    if(attr.length == 2)
                        vs.addParametros(attr[0], attr[1]);
                }
            }

            Object obj = TecniconLookup.lookup("IntegracaoSite", "IntegracaoSite");
            String retorno = (String) obj.getClass().getMethod("trataPost", VariavelSessao.class).invoke(obj, vs);            
            
            out.println(retorno);
            
        } catch (IllegalArgumentException | SecurityException | ExcecaoTecnicon | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex)
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
