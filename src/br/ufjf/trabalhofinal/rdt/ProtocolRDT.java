package br.ufjf.trabalhofinal.rdt;

public class ProtocolRDT {
		
	/***
	 * Tupla de IP destino e porta
	 */
	public int destinationPort;
	public String IP_Destination;
	
	/***
	 * Campos necessários para processamento da porta
	 */
	public int sequence=1;
	public int acknowledgement=0;
	public short ack;
	public byte syn=0;
	public byte fin;
	public int windowSize = 30;
	public String dados;
	public int SEQ; //sequencia vinda da outra parte
	 
	
	public enum Status() {empty, connecting, connected}
	
	public Status status = Status.empty;
	
	public enum headerNames {
		SEQ(0),ACkNOWLEDGEMENT(1), ACK(2),SYN(3), FIN(4), WINDOW(5), DATA(6);
		headerNames(int valor) {
		value = valor;
		}
	}
	
	

	public ProtocolRDT(String IP_Destination, int destinationPort) 
	{
		this.IP_Destination = IP_Destination;
		this.destinationPort = destinationPort;
	}
	
	
	/***
	 * Monta o procotolo para ser enviado
	 * @param data: dados da camada de aplicação
	 * @return retorna o protocolo para ser desmontado do outro lado
	 */
	public String AssembleProtocol(String data)
	{
		String protocol = 
				Seq() + ";" +
				(NextAcknowledgment()) + ";" +
				ack + ";" +
				syn + ";" +
				fin + ";" +
				windowSize + ";";
		return protocol + data;
	}
	
	public String AssembleProtocolAplication(String data)
	{
		String protocol = 
				Seq() + ";" +
				(SEQ+1) + ";" +
				ack + ";" +
				syn + ";" +
				fin + ";" +
				windowSize + ";";
		return protocol + data;
	}
	
	public String AssembleProtocolConfirm(String data)
	{
		String protocol = 
				Seq() + ";" +
				(NextAcknowledgment()+1) + ";" +
				ack + ";" +
				syn + ";" +
				fin + ";" +
				windowSize + ";";
		return protocol + data;
	}
	
	public String AssembleProtocolConfirm(String data, String _acknowledgement)
	{
		String protocol = 
				Seq() + ";" +
				_acknowledgement + ";" +
				ack + ";" +
				syn + ";" +
				fin + ";" +
				windowSize + ";";
		return protocol + data;
	}
	
	/***
	 * Solicitação de Retransmissão do pacote 
	 * @param data
	 * @param retransmit
	 * @return
	 */
	public String AssembleProtocol(String data, boolean retransmit)
	{
		String protocol = 
				Seq() + ";" +
				acknowledgement + ";" +
				ack + ";" +
				syn + ";" +
				fin + ";" +
				windowSize + ";";
		return protocol + data;
	}
	
	
	
	/***
	 * Desmonta o protocolo e repassa os campos para a classe
	 * @param protocolDados
	 * @return retorna os dados para repassar para a camada de aplicação
	 */
	public String[] DisassembleProtocol(String protocolDados)
	{
		String[] p = protocolDados.split(";");
		
		SEQ = Integer.parseInt(p[0]);
		acknowledgement = Integer.parseInt(p[headerNames.ACkNOWLEDGEMENT.value]);
		ack = Short.parseShort(p[2]);
		syn= Byte.parseByte(p[3]);
		fin = Byte.parseByte(p[4]);
		windowSize = Integer.parseInt(p[5]);		
		
		return protocolDados.split(";");
	}
	
	public String[] DisassembleProtocol(String protocolDados, String _acknowledgement)
	{
		String[] p = protocolDados.split(";");
		
		//sequence = Integer.parseInt(p[0]);
		acknowledgement = Integer.parseInt(_acknowledgement);
		ack = Short.parseShort(p[2]);
		syn= Byte.parseByte(p[3]);
		fin = Byte.parseByte(p[4]);
		windowSize = Integer.parseInt(p[5]);		
		
		return protocolDados.split(";");
	}
	

	
	public String DisassembleProtocolTranslate(String protocolDados)
	{
		String[] p = protocolDados.split(";");
		
		String protocolSubtitle = 
				"\nSEQ=" + p[headerNames.SEQ.value] + "\n" +
				"ACKWNOK=" + p[headerNames.ACkNOWLEDGEMENT.value] + "\n" +
				"ACK=" + p[headerNames.ACK.value] + "\n" +
				"SYN=" + p[headerNames.SYN.value] + "\n" +
				"FIN=" + p[headerNames.FIN.value] + "\n" +
				"WIN=" + p[headerNames.WINDOW.value] + "\n";
		
		return protocolSubtitle;
	}
	
	public String OrigemDisassembleProtocol(String[] protocolDados)
	{
		String protocol = 
				protocolDados[headerNames.SEQ.value] + ";" +
				protocolDados[headerNames.ACkNOWLEDGEMENT.value]  + ";" +
				protocolDados[headerNames.ACK.value]  + ";" +
				protocolDados[headerNames.SYN.value]  + ";" +
				protocolDados[headerNames.FIN.value]  + ";" +
				protocolDados[headerNames.WINDOW.value]  + ";";
		return protocol;
	}
	
	/***
	 * Estabelece primeiro passo o 3-way-handshake
	 * @return
	 */
	public String StartConnection()
	{
		this.syn = 1;
		String protocol = AssembleProtocol("");
		
		return protocol;
	}
	public String RespondConnection()
	{
		this.syn = 1;
		this.ack = 1;
		String protocol = AssembleProtocol("");
		
		return protocol;
	}
	
	/***
	 * Solicita encerramento da conexção
	 * @return
	 */
	public String EndConnection()
	{
		this.fin = 1;
		String protocol = AssembleProtocol("");
		
		return protocol;
	}
	
	/***
	 * Conexão estabelecida
	 * @return
	 */
	public String SuccessfulConnection()
	{
		
		this.syn = 0;
		this.ack = 1;
		//this.acknowledgement = Acknowledgment();
		String protocol = AssembleProtocol("");
		
		return protocol;
	}
	
	public int NextAcknowledgment()
	{
		int a = this.SEQ+1;
		return a;
	}
	
	public int Seq()
	{
		int seq = this.sequence++;
		
		return seq;
	}

}
