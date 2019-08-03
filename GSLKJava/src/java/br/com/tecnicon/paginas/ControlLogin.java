package br.com.tecnicon.paginas;

import br.com.tecnicon.controller.EncerarSistema;
import br.com.tecnicon.controller.RepositorioSession;
import br.com.tecnicon.police.ListaSessoes;
import br.com.tecnicon.police.ListaUsuarios;
import br.com.tecnicon.police.SingletonGenerico;
import br.com.tecnicon.police.TValidaSessao;
import br.com.tecnicon.police.Util;
import br.com.tecnicon.server.bd.entity.Sessao;
import br.com.tecnicon.server.dataset.TSQLDataSetTec;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.Stateless;

/**
 *
 * @author ivandro.lessing
 */
@Stateless
public class ControlLogin
{
    
    @Deprecated
    public static void controlSessaoCloud(VariavelSessao vs) throws Exception
    {
        controlSessao(vs);
    }

    public static void controlSessao(VariavelSessao vs) throws Exception
    {   //TODO - Melhorar esse metodo
        ListaUsuarios listaUs = ListaUsuarios.getInstance();

        if (listaUs.getQtdeLogin(vs, vs.getValor("cusuario")) > 0)
        {
            ListaSessoes listaSessoes = ListaSessoes.getInstance();
            VariavelSessao sessaoMatar;
            ConcurrentHashMap<Integer, Sessao> sessoesExcluir = new ConcurrentHashMap<>();

            int s = 0;
            int excluidos = 0;
            Sessao atual;
            for (Entry value : Collections.synchronizedMap(Util.getSessoesEmp(vs)).entrySet())
            {
                atual = (Sessao) value.getValue();
                if (atual.ssessao != -9876
                        && atual.ssessao != -9878
                        && atual.cusuario == Funcoes.strToInt(vs.getValor("cusuario"))
                        && (listaSessoes.obterSessoesUsuario(vs, vs.getValor("cusuario")).size() - excluidos) 
                            > listaUs.getQtdeLogin(vs, vs.getValor("cusuario"))
                        && atual.ssessao != Funcoes.strToInt(vs.getValor("sessao"))
                        && !"P".equals(atual.tipo))
                {
                    sessoesExcluir.put(s++, atual);
                    excluidos++;
                }
            }
            
            for (int i = 0; i < sessoesExcluir.size(); i++)
            {
                sessaoMatar = RepositorioSession.getInstance().getAtribute(sessoesExcluir.get(i).ssessao + "");
                if (sessaoMatar != null)
                {
                    TValidaSessao.eliminarSessao(sessaoMatar, "Controle de limite de login por usuário");
                }
            }
        }
    }

    public static String ValidaQtdeSessoes(VariavelSessao vs, boolean derrubaSess, int tipoVerificacao) throws ExcecaoTecnicon
    {
        //TODO - Melhorar esse metodo
        //tipoVerificacao
        // 1 -> Maior ou igual :: Para Login tradicional
        // 2 -> Maior :: Para verificar após "tela de sessões"
        if (!vs.getValor("tipologin").equals("PAF"))
        {
            Double qtde = (Double) SingletonGenerico.getInstance().retornaObjeto("QTDELICENCAS");
            if (qtde == null)
            {
                TSQLDataSetTec LICENCA = TSQLDataSetTec.create(vs);
                LICENCA.commandText("SELECT COALESCE(LICENCA.QTDELICENCA, 0) AS QTDELICENCA FROM LICENCA");
                LICENCA.open();
                qtde = LICENCA.fieldByName("QTDELICENCA").asDouble();
                SingletonGenerico.getInstance().setaObjeto("QTDELICENCAS", qtde);
            }

            int qtdeSess = RetQtdeSessoes(vs, vs.getValor("sessao"));
            boolean passou = false;

            if (tipoVerificacao == 1)
            {
                passou = qtdeSess >= qtde;
            } else if (tipoVerificacao == 2)
            {
                passou = qtdeSess > qtde;
            }

            if (passou)
            {
                if (derrubaSess)
                {
                    try
                    {
                        new EncerarSistema().encerraSessao(vs, "Quantidade de logins (" + qtdeSess + ") ultrapassou a quantidade de licenças (" + qtde + ")");
                        vs.addParametros("sessao", "-9876");
                    } catch (Exception ex)
                    {
                    }
                }

                return "erro:Com esse login você ultrapassou a quantidade de licenças contratadas do TECNICON Business Suite."
                        + "\n Contate o seu administrador do sistema ou ligue para a TECNICON para adquirir mais licenças de uso: (55) 3537 9800.\n\n";
            }
        }

        return "OK";
    }

    public static int RetQtdeSessoes() throws NumberFormatException, ExcecaoTecnicon
    {
        return RetQtdeSessoes("0");
    }

    public static int RetQtdeSessoes(String sessIgnoraAntesLogin) throws NumberFormatException, ExcecaoTecnicon
    {
        return RetQtdeSessoes(new TVariavelSessao(), sessIgnoraAntesLogin);
    }    
    
    public static int RetQtdeSessoes(VariavelSessao vs, String sessIgnoraAntesLogin) throws NumberFormatException, ExcecaoTecnicon
    {   //TODO - Melhorar esse metodo
        ConcurrentHashMap<Long, Sessao> sess = Util.getSessoesEmp(vs);

        int qtdeSess = 0;
        Long sessaoIg = Long.parseLong(sessIgnoraAntesLogin);

        for (Long l : sess.keySet())
        {
            br.com.tecnicon.server.bd.entity.Sessao s = sess.get(l);
            if (l.equals(Long.parseLong("-9876")) || l.equals(Long.parseLong("-9878"))
                    || (s.tipo != null && s.tipo.equalsIgnoreCase("P"))
                    || (s.tipo != null && s.tipo.equalsIgnoreCase("M"))
                    || s.antesLogin)
            {
                if (s.antesLogin && l.equals(sessaoIg))
                {
                    //Conta igual
                } else
                {
                    continue;
                }
            }
            qtdeSess++;
        }

        return qtdeSess;
    }
}
