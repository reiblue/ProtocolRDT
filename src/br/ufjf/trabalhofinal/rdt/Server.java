package br.ufjf.trabalhofinal.rdt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Server {
	
	public static byte[] receiveData = new byte[1400];
	public static byte[] sendData = new byte[1400];
	public static ArrayList<String> send_packets = new ArrayList<>();
	public static InetAddress IPAddress;
	public static int port;
	public static ProtocolRDT rdtServer;// = new ProtocolRDT("127.0.0.1", porta);
	public static long timeOver = 23000;
	public static ArrayList<String> receivePackets = new ArrayList();
	public static boolean three_way_handshake = false;
	public static ArrayList<String> deliveryApplication = new ArrayList<>();
	public static long ultimoAckConfirmado;
	
	public static void main(String[] args) 
	{
		System.out.println("Programa servidor...\n\n");
		Thread th_send;
		Thread th_receive;
		Thread th_application; //Simula comportamento da camada de aplicação
		Thread th_timerRDT;
		Thread th_confirm;
		
		boolean runThread = true;
		int timeThread = 5;

		boolean solicitation = false;
		
		Semaphore mutexApplication =  new Semaphore(1);
		Semaphore mutexSendPackets =  new Semaphore(1);
		ArrayList<Integer> respondACK = new ArrayList<>();
		ArrayList<String> requestConfirmACK = new ArrayList<>();
		
		try 
		{
			int porta = 9876;
			int numConn = 1;
			
			
			
			DatagramSocket serverSocket = new DatagramSocket(porta);
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			System.out.println("Esperando por datagrama UDP na porta " + porta);
			
			
			
			th_application = new Thread() 
	        {
		       	 @Override
		       	public void run() 
		       	 {
		       		try 
		       		{
		       			String arquivo = "/home/peixoto/Music.mp3";
		       			
		       			//System.out.println("Aplicação em espera\n");
		       			
		       			
		       			
		       			while(runThread)
		       			{
		       				//System.out.println("acquire aplication");
		       				mutexApplication.acquire();
		       				
		       			if(!deliveryApplication.isEmpty())
		       			{
		       				
		       				//System.out.println("Aplicação trabalhando\n");
		       				Iterator<String> it = deliveryApplication.iterator();
		       				
		       				while(it.hasNext())
		       				{
		       					String text = it.next();
		       					
		       					//System.out.println("\n888888\nAplicação valor: " + text);
		       					
		       					if(text.equals("transmition:mp3"))
		       					{
		       						try 
		       						{
		       							mutexSendPackets.acquire();
		       							//System.out.println("acquiere mutexSendPackets");
		       							String packet = rdtServer.AssembleProtocolAplication("send:starting");
										send_packets.add(packet);
										//System.out.println("release mutexSendPackets");
										mutexSendPackets.release();
										
										System.out.println("Aplicação transmitindo\n");
										
										
										
										FileReader file = new FileReader("/home/peixoto/Music.mp3");
							        	 BufferedReader buffer = new BufferedReader(file);
							        	 
							        	 int i = 0;
							        	 String line;
							        	 
							        	 while ((line = buffer.readLine()) != null) {        // Enquanto tem alguma linha pra pegar
							        		 //System.out.println(i + ": " + line);            // Mostra o texto da linha
									        i++;
									        
											packet = rdtServer.AssembleProtocolAplication(line);
											
											mutexSendPackets.acquire();
											//System.out.println("aquire mutexSendPackets");
											send_packets.add(packet);
											//System.out.println("release mutexSendPackets");
											mutexSendPackets.release();
											
											sleep(timeThread * send_packets.size());	
											
									    }
							        	 
									   buffer.close();                                     // Fecha o buffer de leitura
									   file.close(); 
										
									   System.out.println("\nFim da transmissão ");
									   packet = rdtServer.AssembleProtocolAplication("send:end");
									   
									   mutexSendPackets.acquire();
									   //System.out.println("acquire mutexSendPackets");
									   send_packets.add(packet);
									   //System.out.println("release mutexSendPackets");
									   mutexSendPackets.release();
										
									} catch (Exception e) {
										// TODO: handle exception
										e.printStackTrace();
									}
		       					}
		       					else
		       					{
		       						System.out.println("Comando não reconhecido\n");
		       					
		       					}
		       				}
		       				
		       				deliveryApplication.clear();
		       				
		       			}
		       			//System.out.println("release aplication");
		       			mutexApplication.release();
		       			
		       			
		       			
		       			sleep(timeThread);
		       			
		       			}
		       		}catch (Exception e)
		       		{
		       			e.printStackTrace();
		       		}finally {
		       			
					}
		       	 }
	        };	
			
			th_send = new Thread() {
	        	 @Override
	        	public void run() {
	        		 byte[] receiveData = new byte[1400];
	        		 
	        		 while(runThread)
	        		 {
	        			 try 
	        			 {
	        				 mutexSendPackets.acquire();
	        				 //System.out.println("acquire mutexSendPackets");
	        				if(!send_packets.isEmpty())
	        				{
	        					Iterator<String> it = send_packets.iterator();
	        					
	        					while(it.hasNext())
	        					{
	        						String pkt = it.next();	        						
	        						
		       						
		       						/*if(!respondACK.isEmpty() && three_way_handshake)
		       						{
			       						mutex.acquire();				       						
			       						
			       						String[] p = pkt.split(";");
			       						p[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value] = respondACK.get(0).toString();
			       						
			       						pkt = rdtServer.OrigemDisassembleProtocol(p);			       						
			       						
			       						respondACK.remove(0);
			       						
			       						mutex.release();
		       						}*/
	        						
	        						
		       						sendData = pkt.getBytes();
		       						
		       						DatagramPacket sendPacket = new DatagramPacket(sendData,
											sendData.length, IPAddress, port);
								
									serverSocket.send(sendPacket);	
									
									System.out.println("Enviando: " + pkt);
									
									try {
										it.remove();
									} catch (Exception e) {
										// TODO: handle exception
										e.printStackTrace();
									}
									
									
	        					}
	        				}
	        				//System.out.println("release mutexSendPackets");
	        				mutexSendPackets.release();
	        				sleep(timeThread);
	        					
						 } catch (Exception e1) 
	        			 {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}finally {
							mutexSendPackets.release();
						}
	        		 }
	        		
	        	}
	         };
	         
	         th_timerRDT = new Thread()
    		 {	        
	        	 
	        	 @Override
	        	 public void run()
	        	 {
	        		 
	        		 
	        		 long startTime = 0;
	        		 int expectedPacket = 0;
	        		 ArrayList<String> packetOutOfOrder = new ArrayList<>();
	        		 
	        		 int ackServidor = 0;
	        		 int seqServidor = 1;
	        		 
	        		 
	        		 while(runThread)
	        		 {
	        			 try 
	        			 {  
	        				 if(!receivePackets.isEmpty())
	        				 {
	        					 Iterator<String> it = receivePackets.iterator();
	        					 
	        					 while(it.hasNext())
	        					 {
	        						 if(!receivePackets.isEmpty())
	        						 {
	        							 
	        						
	        						 String packet = it.next();
	        						 String[] pkt = rdtServer.DisassembleProtocol(packet);
	        						 System.out.println("\n*********\nN Servidor: " + rdtServer.acknowledgement);
	        						 
	        						 ackServidor = Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]);
	        						 seqServidor = Integer.parseInt(pkt[ProtocolRDT.headerNames.SEQ.value]);
										
	        						 //--------------------------------------------
	        						 /*
	         						long endTime = System.currentTimeMillis();
	         						long timeElapsed = endTime - startTime;
	         						 
	         						 System.out.println("\n*****************\nTempo: " + timeElapsed + "\n******************\n");
	         						 
	         						 if(timeElapsed > timeOver)
	         						 {
	         							//three_way_handshake = false;
	         							//solicitation = false;
	         							send_packets.clear();	         							
	         							
	         							System.out.println("Conexao encerrada, esperando por datagrama UDP na porta " + porta);
	         							continue;
	         						 }
	    	         					*/
	        						 
	        						 if(!three_way_handshake)
	    	        				 {		    	         					
	    	         					
	    	         					//Entende que o cliente deseja estabelecer uma conexão
	    	         					//prepara 	um pacote de conexão para enviar para o cliente
	    	         					if(Integer.parseInt(pkt[ProtocolRDT.headerNames.SYN.value]) == 1 
	    	         							&& Integer.parseInt(pkt[ProtocolRDT.headerNames.ACK.value]) == 0)
	    	         					{
	    	         							         						
	    	         						
	    	         						//rdtServer.acknowledgement = rdtServer.NextAcknowledgment();	
	    	         						
	    	         						packet = rdtServer.RespondConnection(); 
	    	         						
	    	         						mutexSendPackets.acquire();
	    	         						//System.out.println("acquire mutexSendPackets");
	    	         						send_packets.add(packet);
	    	         						//System.out.println("release mutexSendPackets");
	    	         						mutexSendPackets.release();
	    	         						
	    	         						System.out.println("Enviando pacote syn para o cliente...");
	    	         						
	    	         						
	    	         					}
	    	         					//Recebendo aceite de conexão do cliente
	    	         					else if(Integer.parseInt(pkt[ProtocolRDT.headerNames.SYN.value]) == 0 
	    	         							&& Integer.parseInt(pkt[ProtocolRDT.headerNames.ACK.value]) == 1)
	         							{
	    	         						
	    	         						
	    	         						//rdtServer.acknowledgement = rdtServer.NextAcknowledgment();
	    	         						//packet = rdtServer.SuccessfulConnection();
	    	         						//send_packets.add(packet);
	    	         						
	    	         						System.out.println("\n\n&&&&&&&&&&&\nSYN recebido: " + packet + "\n&&&&&&&&&&&&&&");
	    	         							         							         						
	    	         						System.out.println("Conexao estabelecida!");
	    	         						three_way_handshake = true;
	    	         						
	    	         						expectedPacket = rdtServer.acknowledgement;
	    	         						
	    	         						ultimoAckConfirmado = rdtServer.sequence;
	    	         						
	         							}
	    	        				 }else
	    	        				 {
	    	        					 	//Captura ACK para responder ao cliente
											//respondACK.add(rdtServer.acknowledgement);
											
											//if(expectedPacket<=rdtServer.acknowledgement)
											//	expectedPacket = rdtServer.acknowledgement +1;
	    	        					 
	    	        					 System.out.println("\n\nUltimo ack confirmado: " + ultimoAckConfirmado);
										 System.out.println("\nComparar ack: " + pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value] + ">=" + (rdtServer.sequence-1));
	    	        					 
	    	        					 if(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]) >= (rdtServer.sequence-1))
	    	        					 {
	    	        						 //confirmação que recebeu todos os pacotes
	    	        						 requestConfirmACK.clear();
	    	        						 ultimoAckConfirmado = rdtServer.sequence;
	    	        						 System.out.println("Confirmação de todos os pacotes");
	    	        						 
	    	        					 }else if(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]) < rdtServer.sequence)
	    	        					 {
	    	        						 //remove todos os confirmados
	    	        						 if(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]) > ultimoAckConfirmado)
	    	        						 {
	    	        							 ultimoAckConfirmado = rdtServer.sequence;
	    	        							 Iterator<String> i = requestConfirmACK.iterator();
	    	 									
	    	 									while(i.hasNext())
	    	 									{				
	    	 										if(!requestConfirmACK.isEmpty())
	    	 										{
	    	 											String p = i.next(); 
		    	 										
		    	 										String[] comparar = p.split(";");
		    	 										
		    	 										if(Integer.parseInt(comparar[ProtocolRDT.headerNames.SEQ.value]) < ultimoAckConfirmado)
		    	 										{
		    	 											System.out.println("\n\n\nRecebido ack: " + comparar[ProtocolRDT.headerNames.SEQ.value] + ", removendo...\n");
		    	 											i.remove();
		    	 										}
	    	 										} 
	    	 									}
	    	        						 }
	    	        					 }
	    	        					 
	    	        					 String text = pkt[ProtocolRDT.headerNames.DATA.value];
	    	        					 
	    	        					 System.out.println("\n\nEntregando a camada de alplicação: " + text  + "\n");
	    	        					 
	    	        					 //System.out.println("acquire aplication");
	    	        					 mutexApplication.acquire();
	    	        					 deliveryApplication.add(text.trim());
	    	        					 //System.out.println("release aplication");
	    	        					 mutexApplication.release();
	    	        					 
											/*
											//ACK esperado passa para a camada de Aplicação
											if(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]) == expectedPacket)
											{
												
												deliveryApplication.add(pkt[ProtocolRDT.headerNames.DATA.value]);
												expectedPacket = rdtServer.NextAcknowledgment();
												
												System.out.println("!!!!!!!\nACK  esperado...\n!!!!!!");
												
												
											}//Coloca na fila somente os Pacotes novos e ignora os duplicados
											else if(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value])>expectedPacket)
											{
												packetOutOfOrder.add(packet);
												rdtServer.acknowledgement = expectedPacket;		
												
												System.out.println("*****\nACK fora do esperado...\n******");
												System.out.println("Esperado: " + expectedPacket + ", recebido: " + pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]);
											}else
											{
												//Cliente solicita algum pacote
												System.out.println("Pacote chegou tarde!");
											}
											*/
											
											//responde ao servidor que o pacote chegou
	    	        					 	
	    	        					 	String confirmacao = rdtServer.AssembleProtocolConfirm("", seqServidor + "");
	    	        					 	
	    	        					 	mutexSendPackets.acquire();
											send_packets.add(confirmacao);
											
											
											//System.out.println("acquire mutexSendPackets");
											System.out.println("\n\nEnviando confirmação: " + confirmacao);
											//System.out.println("release mutexSendPackets");
											mutexSendPackets.release();
	    	        				 }
	        						 
	        						 //--------------------------------------------
	        						 it.remove();
	        						 }
	        					 }
	        				 }
	        				 
	        				
								
								if(!packetOutOfOrder.isEmpty())
								{
									//ordena pacotes
									Collections.sort(packetOutOfOrder);
									
									Iterator<String> it = packetOutOfOrder.iterator();
									
									while(it.hasNext())
									{
										String packet = it.next();
										String[] ptk = packet.split(";");
										
										if(Integer.parseInt(ptk[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]) == expectedPacket)
										{
											mutexApplication.acquire();
											//System.out.println("acquire aplication");
											deliveryApplication.add(ptk[ProtocolRDT.headerNames.DATA.value]);
											//System.out.println("release aplication");
											mutexApplication.release();
											
											it.remove();
										}
										
									}
								}
        					
	        				 sleep(timeThread/2);
							
						 } catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} 
	        		 }
	        	 }
    	 
    		 };
    		 
    		 th_receive = new Thread() {
	        	 @Override
	        	public void run() 
	        	{
	        		
	        		 boolean solicitation = false;
	        		 
	        		 
	        		 while(runThread)
	        		 {
	        			 try 
	        			 { 
	        				serverSocket.receive(receivePacket);	
        					String packet = new String(receivePacket.getData());        					 
        					receivePackets.add(packet);
	        				 
	        				if(!solicitation)
	     					{
	     						IPAddress = receivePacket.getAddress();
	         					port = receivePacket.getPort();	
	         					
	         					//Guarda tupla {IP, Porta}
	         					rdtServer = new ProtocolRDT(receivePacket.getAddress().toString(), port);	
	         					
	         					solicitation = true;	         					
	         					
	         					th_timerRDT.start();
	     					}
	        				 
	        				//System.out.println("Recebido do cliente: " + rdtServer.DisassembleProtocolTranslate(packet));
	        				System.out.println("Recebendo: " + packet);       				
        					System.out.println("____________________________\n");
	        				
        					sleep(timeThread);
							
						 } catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	        			 
	             		
	        		 }
	        		 
	        	}
	         };
	         
	         th_confirm = new Thread() 
				{
					@Override
					public void run()
					{
						try 
						{
							while(runThread) 
							{
								
								mutexSendPackets.acquire();
								if(!requestConfirmACK.isEmpty())
								{
									Iterator<String> it = requestConfirmACK.iterator();
									
									while(it.hasNext())
									{				
										mutexSendPackets.acquire();
										//System.out.println("acquire mutexSendPackets");
										String pkt = it.next();
										send_packets.add(pkt);
										//System.out.println("release mutexSendPackets");
										mutexSendPackets.release();
										
										System.out.println("\n################th_confirm: " + pkt + "\\n################");
									}
									
								}
								mutexSendPackets.release();
								
								sleep(timeThread);
							}
							
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				};
	         
	         th_receive.start();
			
	         th_send.start();
	         
	         th_confirm.start();
	         
	         th_application.start();
	         
			//---------------------------------------------------------------------------
			/*
	        DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			System.out.println("Esperando por datagrama UDP na porta " + porta);
			
			serverSocket.receive(receivePacket);
			System.out.print("Datagrama UDP [" + numConn + "] recebido...");

			String packet = new String(receivePacket.getData());
			System.out.println(packet);
			
			
			
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();	
			
			//Guarda tupla {IP, Porta}
			rdt = new ProtocolRDT(receivePacket.getAddress().toString(), port);				
			String[] pkt = rdt.DisassembleProtocol(packet);
			
			//Estabelece conexão
			if(Integer.parseInt(pkt[3]) == 1)
			{
				sendData = rdt.StartConnection().getBytes();

				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, IPAddress, port);
				
				serverSocket.send(sendPacket);
			}
			
			serverSocket.receive(receivePacket);
			

			while (true) {

				serverSocket.receive(receivePacket);
								
				
				//*************************************************************************
				
				String capitalizedSentence = packet.toUpperCase();

				sendData = capitalizedSentence.getBytes();

				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, IPAddress, port);
				
				System.out.print("Enviando " + capitalizedSentence + "...");

				serverSocket.send(sendPacket);
				System.out.println("OK\n");	
				
			}
			*/
			
			
		} catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}
		

	}

}
