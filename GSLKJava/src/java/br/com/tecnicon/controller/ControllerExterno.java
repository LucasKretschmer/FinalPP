/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.controller;

import br.com.tecnicon.criptografia.PWSec;
import br.com.tecnicon.server.Propriedades;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import br.com.tecnicon.utils.system.SystemRuntime;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.util.Date;
import java.util.TreeMap;
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
@WebServlet(name = "ControllerExterno", urlPatterns =
{
    "/ControllerExterno"
})
public class ControllerExterno extends HttpServlet
{

    public static void main(String[] args) throws Exception 
    {
        String x = "LX7olOPZ5Xm7p+PXrd9DRQR7z+5DWH7YhM4Ac4aAjlj1ay+QbewH31Fnp7CIgZNJr+gGmK30EyWGS7hGO6zAN984wp3oM6FqgtUC4Jbn+oNxF0PEl9P+g6x8wP0gXAePkMxPtQCdzdSkWlXizj2/X/Uu8GNI+w3IaqDOfR5l+EIsYBQcmTsPrKEmd/jPhHR2ebVyjrkSNIgcQovMZpcAKhRsKU3eDp/xrhiW1oG23nmr1zX6bIowlR0lqIixgkqh5JsdMhfFH4X9urtopz/VdgskeDh5iv6z8qtq6wVPT3feTPwAdHmzahAg+vqXX+PQJd7hYEfQMV5g/tcO2N/KWe9Q5IiS5SmG";
        System.out.println(PWSec.decrypt(x));
    }
    
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

            try
            {
                //TODO gravar numa tabela os request que chegam no portal para limitar a abrangencia de EJBs a serem utilizados
                String parametros = "";
                String dest = "Ead";
                if (request.getParameter("s") != null && Propriedades.isServidorPortal())
                {
                    parametros = request.getParameter("s");
                    dest = "Suporte";
                } else if (Propriedades.isServidorPortal() && request.getParameter("d") != null)
                {
                    //Ead
                    parametros = request.getParameter("d");
                } else if (request.getParameter("p") != null)
                {
                    //Pesquisas
                    parametros = request.getParameter("p");
                } else if (request.getParameter("dh") != null)
                {
                    //Atualizador
                    parametros = request.getParameter("dh");

                    Date dt = Funcoes.strToDateTime(new TVariavelSessao(), parametros);
                    Date agora = new Date();
                    if (Funcoes.compararData(dt, "=", agora))
                    {
                        mataJava();
                    } else
                    {
                    }
                } else
                {
                    throw new Exception("Você não possui permissão para acessar este recurso");
                }

                parametros = PWSec.decrypt(parametros);
                if (parametros != null)
                {
                    parametros = parametros.replace("|", "&");
                }
                
                //TODO - Tudo tem que passar pela controller
                RequestDispatcher r = request.getRequestDispatcher(dest + "?" + parametros);
                r.forward(request, response);

            } catch (Exception ex)
            {
                new ExcecaoTecnicon(new TVariavelSessao(), ex);
                out = escreve(out, response, "erro:" + ex.getMessage());
            }

        } finally
        {
            out.close();
        }
    }

    private PrintWriter escreve(PrintWriter out, HttpServletResponse response, String mensagem) throws IOException
    {
        out = response.getWriter();
        out.println(mensagem);
        out.close();
        return out;
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

    private void mataJava() throws ExcecaoTecnicon
    {
        StringBuilder sb = new StringBuilder();
        TreeMap<Double, String> procs = new TreeMap<>();
        boolean achou = false;

        sb.append(SystemRuntime.exec("top -b -n 1"));

        String linhas[] = sb.toString().split("\n");
        String tmp[];
        int z;
        for (int i = 0; i < linhas.length; i++)
        {
            tmp = linhas[i].trim().split(" ");
            if (tmp.length > 0 && tmp[0].trim().equalsIgnoreCase("PID"))
            {
                achou = true;
                continue;
            }

            if (achou)
            {
                if (tmp[tmp.length - 1].equalsIgnoreCase("java"))
                {

                    z = 0;
                    for (int x = 0; x < tmp.length; x++)
                    {
                        if (tmp[x].trim().equals(""))
                        {
                            continue;
                        }

                        if (z == 9)
                        {
                            procs.put(Funcoes.strToDouble(tmp[x]), tmp[0]);
                        }

                        z++;
                    }
                }
            }

        }

        for (Double d : procs.descendingKeySet())
        {
            SystemRuntime.exec("kill -9 " + procs.get(d));
            break;
        }
    }
}
