package br.com.tecnicon.controller;

import br.com.tecnicon.objects.put.DadosEmpresa;
import br.com.tecnicon.paginas.Index;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author sipmann
 */
@WebServlet(name = "Empresa", urlPatterns =
{
    "/Empresa/*"
})
public class Empresa extends HttpServlet
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
            //HttpSession session = request.getSession();

            TVariavelSessao vs = new TVariavelSessao();
            vs.addParametros("sessao", "-9876");

            vs.addParametros("USACONTEXT", "N");

            String pathInfo = request.getPathInfo();
            /**
             * Quando chega a requisição sem informar a empresa: </ br>
             * URL: protocolo://url:port/Tecnicon/Empresa
             */
            if (pathInfo == null || "".equals(pathInfo))
            {
                throw new Exception("A Razão Social deve ser informada na URL! Favor Verificar.");
            }

            /*
             * Posição:        Valor:
             *  [0]             (vazio)
             *  [1]             Razão Social da Empresa
             *  [2]             Tipo de Login(opcioal) [Cliente, Fornecedor, Colaborador, Representante]
             */
            String[] paths = pathInfo.split("/");

            String empresa;
            String tipologin;

            /**
             * Quando chega a requisição sem informar a empresa: </ br>
             * URL: protocolo://url:port/Tecnicon/Empresa/
             */
            if (paths.length == 0)
            {
                throw new Exception("A Razão Social deve ser informada na URL! Favor Verificar.");
            }

            empresa = paths[1];
            tipologin = "L"; //L = tipo de login normal, semelhante a /Tecnicon/Portal

            if (paths.length > 2)
            {
                tipologin = paths[2].toLowerCase();
                switch (tipologin)
                {
                    case "cliente":
                    case "fornecedor":
                    case "colaborador":
                    case "representante":
                        break;
                    default:
                        tipologin = "L";
                        break;
                }
            }

            if (getRotasEspeciais().contains(empresa))
            {
                response.sendRedirect("/Tecnicon" + pathInfo);
                return;
            }

            String cempresa = carregaEmpresa(vs, empresa);
            vs.addParametros("empresa", cempresa);
            vs.addParametros("tipologin", tipologin);
            vs.addParametros("ipcliente", request.getRemoteAddr());
            vs.addParametros("USACONTEXT", "S");

            //session.setAttribute("vs", vs);
            vs.setParametrosSessao(vs.getListaParametroSessao());

            //RepositorioSession.getInstance().setAttribute(session.getId(), vs);
            //session.setAttribute("navegador", "chrome");
            //session.setAttribute("versao", "50");
            String abreChat = request.getParameter("abreChat");
            String usuario = "";
            String datahora = "";

            if (abreChat != null && !abreChat.equals("") && abreChat.split("§").length > 2)
            {
                usuario = abreChat.split("§")[1];
                datahora = abreChat.split("§")[2];
                abreChat = abreChat.split("§")[0];

                if ((abreChat + datahora).equals(ValidaParametro.getInstance().getChat(usuario + "-" + vs.getValor("empresa"))))
                {
                    ValidaParametro.getInstance().setChat(usuario + "-" + vs.getValor("empresa"), "");
                    response.sendRedirect("/Tecnicon/Empresa/" + pathInfo);
                } else
                {
                    ValidaParametro.getInstance().setChat(usuario + "-" + vs.getValor("empresa"), abreChat + datahora);
                }
            } else
            {
                abreChat = "";
            }

            if (abreChat != null && !abreChat.equals(""))
            {
                out.println(Index.retornaIndex("navegador", "false", "", "false", tipologin, request, abreChat));
            } else
            {
                out.println(Index.retornaIndex("navegador", "false", "", "false", tipologin, request));
            }

            out.close();

        } catch (Exception ex)
        {
            out.println("erro:" + ex.getMessage());
            out.close();
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

    private String carregaEmpresa(TVariavelSessao vs, String razaoSocial) throws Exception
    {
        TClientDataSet empresas = DadosEmpresa.getEmpresas("RAZAO_SOCIAL = '" + razaoSocial + "'");
        if (empresas.isEmpty())
        {
            throw new Exception("Empresa " + razaoSocial + " não localizada. ");
        }

        return empresas.fieldByName("CEMPRESA").asString();
    }

    /**
     * Adicionar nesta lista as rotas que devem receber tratamento diferenciado
     */
    private List<String> getRotasEspeciais()
    {
        return Arrays.asList("images", "RetornaImg", "Controller");
    }

}
