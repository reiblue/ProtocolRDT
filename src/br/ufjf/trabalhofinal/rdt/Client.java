package br.ufjf.trabalhofinal.rdt;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Client {

	public static byte[] receiveData = new byte[1400];
	public static byte[] sendData = new byte[1400];
	public static ArrayList<String> send_packets = new ArrayList<>();
	public static InetAddress IPAddress;
	public static int port;
	public static ProtocolRDT rdtClient;// = new ProtocolRDT("127.0.0.1", porta);
	public static long timeOver = 23000;
	public static ArrayList<String> receivePackets = new ArrayList<>();	
	public static ArrayList<String> deliveryApplication = new ArrayList<>();
	public static boolean three_way_handshake = false;
	public static int ultimoAckConfirmado;
	
	public enum AppTransmition {start, stopped, transmitining, endTransmitining}

	public static void main(String[] args) {
		try 
		{
			
		
			System.out.println("Programa Cliente...\n\n");
			Thread th_send;
			Thread th_receive;
			Thread th_application; //Simula comportamento da camada de aplicação
			Thread th_timerRDT;
			Thread th_confirm;
			
			boolean runThread = true;
			int timeThread = 5;
			rdtClient = new ProtocolRDT("127.0.0.1", port);
			
			DatagramSocket clientSocket = new DatagramSocket();
			String servidor = "localhost";
			port = 9876;	
			IPAddress = InetAddress.getByName(servidor);
			
			Semaphore mutex =  new Semaphore(1);
			
			ArrayList<Integer> respondACK = new ArrayList<>();
			ArrayList<String> requestConfirmACK = new ArrayList<>();
			
			
			th_application = new Thread() 
	        {
		       	 @Override
		       	public void run() 
		       	 {
		       		try 
		       		{
		       			AppTransmition status = AppTransmition.stopped;
		       			
		       			//if(rdtClient.status == ProtocolRDT.Status.connected)
		       			{
		       				
		       				//envia solicitação para receber o arquivo
		       				String packet = rdtClient.AssembleProtocolAplication("transmition:mp3");
		       				
		       				send_packets.add(packet);
		       				requestConfirmACK.add(packet);
		       				
		       				//System.out.println("\n%%%%%%%\nAplicação: " + packet + "\n%%%%%%%%");
		       				
		       				String nameFile = "music3.mp3";
		       				
		       				File file = new File(nameFile);
		       				
		       				if(!file.exists())
		       				{
		       					file.createNewFile();
		       				}else
		       				{
		       					file.delete();
		       					file.createNewFile();
		       				}	
		       						
		       				FileWriter fileWriter = new FileWriter(nameFile);                // Abre um arquivo para escrita
		       				BufferedWriter bufferWriter = new BufferedWriter(fileWriter);   // Cria um buffer de escrita
		       				//bufferWriter.write(args[1]);                                    // Grava no arquivo o texto do segundo parâmetro
		       				//bufferWriter.close();                                           // Fecha o buffer
		       				//fileWriter.close();                                             // Fecha o arquivo
		       				
		       				while(runThread)
		       				{	    
		       					
		       					Iterator<String> received = deliveryApplication.iterator();
		       					
		       					while(received.hasNext())
		       					{
		       						String text = received.next();
		       						String[] pkt = text.split(";");
		       								       						
		       						
		       						switch (status) {
									case stopped: 
										
										if(text.trim().equals("send:starting"))
			       						{
											System.out.println("******************************************************");
			       							System.out.println("***********Recebendo arquivo**************************");
			       							System.out.println("******************************************************");
			       							status = AppTransmition.start;
			       							bufferWriter = new BufferedWriter(fileWriter);
											status = AppTransmition.transmitining;
			       							continue;			       							
			       						}
										
										 
										break;
									case start:
										System.out.println("******************************************************");
		       							System.out.println("***********Preparando arquivo*************************");
		       							System.out.println("******************************************************");
										bufferWriter = new BufferedWriter(fileWriter);
										status = AppTransmition.transmitining;
										break;
									case transmitining:
										
										if(text.trim().equals("send:end"))
			       						{
											System.out.println("******************************************************");
			       							System.out.println("***********Arquivo recebito completo******************");
			       							System.out.println("******************************************************");
			       							status = AppTransmition.endTransmitining;
			       								       							
			       						}else 
			       						{
			       							System.out.println("Gravando");
			       							bufferWriter.write(args[1]); 
			       						}
										break;	
									default:
										System.out.println("******************************************************");
		       							System.out.println("***********Comando não reconhecido********************");
		       							System.out.println("******************************************************");
										break;										
									}
		       						
		       					}	
		       					
		       					deliveryApplication.clear();
		       					
		       					switch(status)
		       					{
		       					case endTransmitining:
									bufferWriter.close();
				       				fileWriter.close(); 
				       				status = AppTransmition.stopped;
				       				break;
		       					}
		       					
		       					sleep(timeThread);
		       				}
		       				
		       				
		       				
		       			}
		       			
		       			
		       			
		       		}catch (Exception e)
		       		{
		       			e.printStackTrace();
		       		}
		       	 }
	        };	       

	        
			th_receive = new Thread() {
		       	 @Override
		       	public void run() {
		       		 	
		       		 while(runThread)
		       		 {
		       			 try 
		       			 {
		       				String packet;
		       				BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
		       						System.in));		       				 
		       				 
		       				 {
		       					//System.out.println("Pacote recebido...");
		       					
		       					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		       					clientSocket.receive(receivePacket);
		       					
		       					packet = new String(receivePacket.getData());		       					
		       							       				 
		       					receivePackets.add(packet);
		       					
		       					//System.out.println("Recebido: " + rdtClient.DisassembleProtocolTranslate(packet));
		        				System.out.println("Recebido: " + packet);       				
	        					System.out.println("____________________________\n");
		       				 }
		   					
		       				 	sleep(timeThread);
								
							 } catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}	
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
			       						
			       						
			       						
			       						pkt = rdtClient.OrigemDisassembleProtocol(p);			       						
			       						
			       						respondACK.remove(0);
			       						
			       						mutex.release();
		       						}*/
		       						
		       								       						
		       						
		       						sendData = pkt.getBytes();
		       						
		       						DatagramPacket sendPacket = new DatagramPacket(sendData,
											sendData.length, IPAddress, port);
								
		       						
									clientSocket.send(sendPacket);
									
																		
									//System.out.println("Enviando: " + rdtClient.DisassembleProtocolTranslate(pkt));
									System.out.println("Enviando: " + pkt);
									
									it.remove();
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
	        
	      //Controla o congestionamento e a entrega ordenada de pacotes
			th_timerRDT = new Thread()
			{
				@Override
				public void run()
				{
										
					try 
					{						
			       		 //boolean solicitation = false;
			       		 long startTime = 0;
			       		 //------------------------------------------------------
			       		 
						ArrayList<String> packetOutOfOrder = new ArrayList<>();
						int expectedPacket = 0;
						int ackServidor = 0;
						int seqServidor = 1;
								
						
						while(runThread)
						{							
							
							if(!receivePackets.isEmpty())
							{
								Iterator<String> it = receivePackets.iterator();
								
								while(it.hasNext())
								{
									String packet = it.next();
									String[] pkt = rdtClient.DisassembleProtocol(packet);
									
									//System.out.println("\n*********\nN Servidor: " + rdtClient.acknowledgement);
									
									ackServidor = Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]);
									seqServidor = Integer.parseInt(pkt[ProtocolRDT.headerNames.SEQ.value]);
									
									if(!three_way_handshake)
				       				 {
				       					
			        					//Entende que o Servidor aceita estabelecer uma conexão
			        					//prepara um pacote de conexão para enviar para o cliente
			        					if(Integer.parseInt(pkt[ProtocolRDT.headerNames.SYN.value]) == 1 
			        							&& Integer.parseInt(pkt[ProtocolRDT.headerNames.ACK.value]) == 1)
			        					{
			        						
			        						//rdtClient.acknowledgement++;
			        						packet = rdtClient.SuccessfulConnection();
			        						send_packets.add(packet);
			        						
			        						sleep(1000);
			        						
			        						System.out.println("Conectado...");
			        						three_way_handshake = true;
			        						rdtClient.status = ProtocolRDT.Status.connected;
			        						
			        						//expectedPacket = rdtClient.acknowledgement+1;
			        						
			        						//respondACK.add(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]));
			        						
			        						//inicia a aplicação
			        						th_application.start();
			        						
			        						System.out.println("\n&&&&&&&&&&&\nSYN recebido: " + packet + "\n&&&&&&&&&&&&&&");
			        						
			        						ultimoAckConfirmado = rdtClient.sequence;
			        					}
				       				 }
									//-------------------------------------------------------------------------------------------------------------									
									else
				       				 {
										//Captura ACK para responder
										//respondACK.add(rdtClient.acknowledgement);
										
										/*if(expectedPacket<=rdtClient.acknowledgement)
											expectedPacket = rdtClient.acknowledgement +1;*/
										/*expectedPacket = rdtClient.SEQ +1;
										
										//SEQ esperada, passa para a camada de Aplicação
										if(seqServidor == expectedPacket)
										{
											
											deliveryApplication.add(pkt[ProtocolRDT.headerNames.DATA.value]);
											expectedPacket = rdtClient.NextAcknowledgment();
											
											
										}//Coloca na fila somente os Pacotes novos e ignora os duplicados
										else if(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value])>expectedPacket)
										{
											packetOutOfOrder.add(packet);
											rdtClient.acknowledgement = expectedPacket;
											
											System.out.println("*****\nACK fora do esperado...\n******");
										}else
										{
											//Cliente solicita algum pacote
										}*/
										System.out.println("\n\nUltimo ack confirmado: " + ultimoAckConfirmado);
										System.out.println("\nComparar ack: " + pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value] + ">=" + (rdtClient.sequence-1));
										
										if(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]) >= (rdtClient.sequence - 1))
	    	        					 {
	    	        						 //confirmação que recebeu todos os pacotes
	    	        						 requestConfirmACK.clear();
	    	        						 ultimoAckConfirmado = rdtClient.sequence;
	    	        						 System.out.println("Confirmação de todos os pacotes");
	    	        						 
	    	        					 }else if(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]) < rdtClient.sequence)
	    	        					 {
	    	        						 //remove todos os confirmados
	    	        						 if(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]) > ultimoAckConfirmado)
	    	        						 {
	    	        							 ultimoAckConfirmado = rdtClient.sequence;
	    	        							 Iterator<String> i = requestConfirmACK.iterator();
	    	 									
	    	 									while(i.hasNext())
	    	 									{				
	    	 										if(!requestConfirmACK.isEmpty())
	    	 										{
	    	 											String p = i.next(); 
		    	 										
		    	 										String[] comparar = p.split(";");
		    	 										
		    	 										System.out.println("\n\n\nComparar ack: " + comparar[ProtocolRDT.headerNames.SEQ.value] + ">" + ultimoAckConfirmado);
		    	 										
		    	 										if(Integer.parseInt(comparar[ProtocolRDT.headerNames.SEQ.value]) < ultimoAckConfirmado)
		    	 										{
		    	 											System.out.println("\n\n\nRecebido ack: " + comparar[ProtocolRDT.headerNames.SEQ.value] + ", removendo...\n\n");
		    	 											i.remove();
		    	 										}
	    	 										} 
	    	 									}
	    	        						 }
	    	        					 }
										
										
										String text = pkt[ProtocolRDT.headerNames.DATA.value];
										
										//responde ao servidor que o pacote chegou
										send_packets.add(rdtClient.AssembleProtocolConfirm("", seqServidor + ""));
										
										System.out.println("\n\nEntregando a camada de alplicação: " + text  + "\n");
	    	        					 
	    	        					 
	    	        					 mutex.acquire();
	    	        					 deliveryApplication.add(text);
	    	        					 mutex.release();
										
				       				 }
									
									mutex.acquire();
									
									Iterator<String> conf = requestConfirmACK.iterator();
									
									//confirmação recebida
									while(conf.hasNext())
									{
										String p = conf.next();
										String[] comparar = p.split(";");
										
										if(Integer.parseInt(pkt[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]) > Integer.parseInt(comparar[ProtocolRDT.headerNames.ACkNOWLEDGEMENT.value]))
										{
											conf.remove();
											break;
										}
										
										
									}
									
									it.remove();
									
									mutex.release();
								} //end while
							}
							
							//Responde com ACK
							if(!respondACK.isEmpty())
							{
								if(!packetOutOfOrder.isEmpty())
								{
									//Solicita retransmição
									//rdtClient.acknowledgement = expectedPacket;
									//String retransmit = rdtClient.AssembleProtocol("", true);
									
									//send_packets.add(retransmit);
									
									//System.out.println("Solicita retransmição");
									
								}else
								{
									/*
									mutex.acquire();
									Iterator<Integer> it = respondACK.iterator();
									int maxValuePacket = 0;
									
									//procura o valor do maior pacote dentro de ordem
									while(it.hasNext())
									{
										maxValuePacket = it.next();
									}
									
									//confirma que recebeu o maior pacote
									//rdtClient.acknowledgement = maxValuePacket;
									
									System.out.println("Maximo valor do cliente: " + maxValuePacket);
									send_packets.add(rdtClient.AssembleProtocolConfirm("", maxValuePacket + ""));
									
									it.remove();
									
									mutex.release();
									*/
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
										deliveryApplication.add(ptk[ProtocolRDT.headerNames.DATA.value]);
										it.remove();
										break;										
									}
									
								}
							}
							
							sleep(timeThread);
						}
						
						
						
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
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
							
							mutex.acquire();
							if(!requestConfirmACK.isEmpty())
							{
								Iterator<String> it = requestConfirmACK.iterator();
								
								while(it.hasNext())
								{									
									String pkt = it.next();
									send_packets.add(pkt);
									System.out.println("Tempo estourado enviando pacote\n");
								}
								
							}
							mutex.release();
							
							sleep(timeThread*8);
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
	        
			
			while(true)
			{
				System.out.println("Digite o 'conexao' para conectar ao servidor: ");
		        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
						System.in));
		        
		        send_packets.add(rdtClient.StartConnection());
		        
		        th_timerRDT.start();
		        
		        break;
		        /*
				String sentence = inFromUser.readLine();
				
				if(sentence.equals("conexao"))
				{
					send_packets.add(rdt.StartConnection());
					break;
				}else
				{
					System.out.println("Ops! Não entendi, vamos tentar novamente?");
				}*/
			}
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
