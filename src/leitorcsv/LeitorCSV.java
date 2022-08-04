/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package leitorcsv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Usuario
 */
public class LeitorCSV {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException, SQLException {

        String url = "jdbc:postgresql://localhost/makesystem?currentSchema=csv";
        Properties props = new Properties();
        props.setProperty("user","andrei");
        props.setProperty("password","andrei1234");
        props.setProperty("ssl","false");
        Connection conn = (Connection) DriverManager.getConnection(url, props);
        
        //Caminho do arquivo
        String csvArquivo = "C:\\Users\\Usuario\\Downloads\\desafio_junior.csv";

        // classe que le o arquivo todo e joga pra memoria.
        BufferedReader conteudoCSV = null;

        String linha = "";

        String csvSeparadorCampo = ";";

        try {
            conteudoCSV = new BufferedReader(new FileReader(csvArquivo));
            conteudoCSV.readLine();
            int contadorIdade = 0;
            int contaPessoaValida = 0;
            int contaPj = 0;
            int contaPf = 0;
            int contaDuplicados = 0;
            int contaSp = 0;
            int contaInvalido = 0;
            int contaRegistros =0;

            while ((linha = conteudoCSV.readLine()) != null) {
                contaRegistros++;
                boolean pessoaValida = true;
                String[] conteudo = linha.split(csvSeparadorCampo);

                String nome = conteudo[0];
                String documento = conteudo[1].replace(".", "").replace("-", "").replace(" ", "");
                String nasc = conteudo[2];
                String fone = conteudo[3].replace(".", "").replace("-", "").replace("(", "").replace(")", "").replace("*", "").replace(" ", "");

                String nomeSplitado[] = nome.split(" ");

                // validando Nome
                if (nomeSplitado.length >= 2) {
                    for (String tempNome : nomeSplitado) {
                        if (tempNome.length() < 2) {
                            System.out.println("Pessoa invalida:" + nome);
                            pessoaValida = false;
                        }
                    }
                } else {
                    pessoaValida = false;
                    //System.out.println("Pessoa invalida:" + nome);
                    continue;
                }
                
                // validando documento
                if (documento.length() != 11 && documento.length() != 14) {
                    pessoaValida = false;
                    
                    //System.out.println("Pessoa invalida:" + nome);
                }
                
                
                // validando Idade e data nascimento
                try {
                    contadorIdade += getIdade(conteudo[2]);
                } catch (ParseException e) {
                    pessoaValida = false;
                    //contaInvalido++;
                    //System.out.println("Pessoa invalida:" + nome);
                }
                
                // valida telefone
                
                if (fone.length() != 11 ){
                    pessoaValida = false;
                    //System.out.println("Pessoa invalida:" + nome);
                }
                
                if (pessoaValida) {
                    if (documento.length() == 11){
                        contaPf++;
                    }
                    if (documento.length() == 14){
                        contaPj++;
                    }
                    
                    contaPessoaValida++;
               
                    char tempDddPrimeiro = fone.charAt(0);
                    char tempDddSegundo = fone.charAt(1);
                    
                    if(tempDddPrimeiro == '1' && tempDddSegundo == '1'){
                        contaSp++;
                    }
                    
                    Statement stmt = null;
                    stmt = conn.createStatement();
                    if (stmt.execute ("select * from csv.pessoa where pessoa.documento = '\"+documento+\"'") == true ){
                        //System.out.println("Já possui registro:" + nome);
                        contaDuplicados++;
                    }else{
                        stmt.execute("INSERT INTO pessoa VALUES ('"+nome+"', '"+documento+"', '"+new Date(nasc)+"', '"+fone+"')");
                        System.out.println( nome+ "Registrado com Sucesso:");
                        
                    }
                    //stmt.execute("INSERT INTO pessoa VALUES ('"+nome+"', '"+documento+"', '"+new Date(nasc)+"', '"+fone+"')");
                    //System.out.println( nome+ "Registrado com Sucesso:");
                }
            }
            
            
            //System.out.println("Total idade acumulada:" +contadorIdade);
            //System.out.println("total linhas lidas: "+contaLinha);
            System.out.println("Media de idades: "+(contadorIdade / contaPessoaValida));
            System.out.println("Quantidade de Pessoas Fisicas: "+contaPf);
            System.out.println("Quantidade de Pessoas Juridicas: "+contaPj);
            System.out.println("Quantidade de registros invalidos: "+(contaRegistros - contaPessoaValida));
            System.out.println("Quantidade de registros duplicados: "+contaDuplicados);
            System.out.println("Quantidade de telefones de SP: "+contaSp);
            
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado: \n" + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.print("IndexOutOfBounds: \n" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Erro: \n" + e.getMessage());
        } finally {
            if (conteudoCSV != null) {
                try {
                    conteudoCSV.close();
                } catch (IOException e) {
                    System.out.println("IO Erro \n" + e.getMessage());
                }
            }
        }

    }

    public static Integer getIdade(String nasc) throws ParseException {
        Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(nasc);
        GregorianCalendar hj = new GregorianCalendar();
        GregorianCalendar nascimento = new GregorianCalendar();
        if (nasc != null) {
            nascimento.setTime(date1);
        }
        int anohj = hj.get(Calendar.YEAR);
        int anoNascimento = nascimento.get(Calendar.YEAR);
        return new Integer(anohj - anoNascimento);
    }

}
