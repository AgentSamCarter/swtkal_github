package swtkal.swing.client;
//Achtung: im Wesentlichen unveraendert aus Calendarium uebernommen
// TODO Zugriff auf Images so abaendern, dass auch die Images in einer Jar-Datei liegen duerfen!

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

import swtkal.client.Client;
import swtkal.domain.Person;
//import swtkal.domain.Person;
import swtkal.domain.Termin;
//import swtkal.exceptions.PersonException;
import swtkal.exceptions.TerminException;
//import swtkal.swing.elements.person.LoginDialog;
import swtkal.server.SimpleServer;

public class SwingClient extends Client implements ActionListener
{
	private static final long serialVersionUID = -3226733188150527572L;

	public static void main(String[] args)
	{
		//====================================================
		SimpleServer test = new SimpleServer();
		
		// test Eindeutigkeit id
		
		for (var entry : test.teilnehmerTermine.entrySet()) {
			Map<String, Vector<Termin>> mapTerm= entry.getValue();
			for (var entryTwo : mapTerm.entrySet()) {
				Vector<Termin> vector = entryTwo.getValue();
				for(Termin obj : vector)
				{
				    System.out.println(obj.getTerminID());
				    System.out.println(obj.getId());
				}
			}
		}
		
		//test isTerminIdKnown
		System.out.println(test.isTerminIdKnown(3));
		
		//test getTermin
		try {
			System.out.println(test.getTermin(1));
		} catch (TerminException e) {
			e.printStackTrace();
		}	
		    
		//test delete
		
		System.out.println("bevor dem Delete vom Termin 1: " + test.isTerminIdKnown(1));
		try {
			test.delete(1);
		} catch (TerminException e) {
			e.printStackTrace();
		}
		System.out.println("Nach dem Delete: vom Termin 1: " +test.isTerminIdKnown(1));
		
		
		// Real Code
		new SwingClient().frame.setVisible(true);
	}

	protected JFrame frame;
	protected final int INITIAL_WIDTH  = 850;
	protected final int INITIAL_HEIGHT = 700;
	protected JLayeredPane layer = new JLayeredPane();
	protected JLabel statusLabel;
	protected Tagesansicht tagesansicht;

   // cache for internal frames
   protected Hashtable<String, JInternalFrame> frames = new Hashtable<>();

	protected SwingClient()
	{
		server = swtkal.server.Server.getServer();
		server.startServer();
		
		// Look & Feel
		try
		{
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());			// *** use this for MacOS
		}
		catch (Exception exc)
		{
			System.err.println("Error loading L&F: " + exc);
		}

		// connect
		try
		{
			frame = new JFrame();
			//LoginDialog connect = new LoginDialog(frame, server);
			user = new Person("SWTKal", "Admin", "ADM"); 
			//user = connect.getUser();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			server.stopServer();
			System.exit(0);
		}
				
		frame.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(screenSize.width/2 - INITIAL_WIDTH/2,
				      screenSize.height/2 - INITIAL_HEIGHT/2);
		frame.setTitle("SWTKal-SwingClient");

		createMenu();
		createContent();

		frame.addWindowListener(new WindowEventHandler());
	}

	protected void createMenu()
	{
		JMenuBar mbar = new JMenuBar();

		JMenu mEintraege = new JMenu("Eintragen");
		mEintraege.setMnemonic('E');
		mbar.add(mEintraege);

			JMenuItem iTermin = new JMenuItem("Termin eintragen");
			iTermin.setMnemonic('T');
			iTermin.setActionCommand("Termin");
			iTermin.addActionListener(this);
			mEintraege.add(iTermin);
	
			JMenuItem iToDo = new JMenuItem("ToDo eintragen");
			iToDo.setMnemonic('D');
			iToDo.setActionCommand("ToDo");
			iToDo.setEnabled(false);
			iToDo.addActionListener(this);
			mEintraege.add(iToDo);
	
			JMenuItem iClear = new JMenuItem("Datens?uberung");
			iClear.setMnemonic('S');
			iClear.setActionCommand("L?schen");
			iClear.setEnabled(false);
			iClear.addActionListener(this);
			mEintraege.add(iClear);

		JMenu mAnsehen = new JMenu("Ansehen");
		mAnsehen.setMnemonic('A');
		mbar.add(mAnsehen);

			JMenuItem iTag = new JMenuItem("Tag");
			iTag.setMnemonic('T');
			iTag.setActionCommand("Tag");
			iTag.addActionListener(this);
			mAnsehen.add(iTag);

			JMenuItem iWoche = new JMenuItem("Woche");
			iWoche.setMnemonic('W');
			iWoche.setActionCommand("Woche");
			iWoche.setEnabled(false);
			iWoche.addActionListener(this);
			mAnsehen.add(iWoche);

			JMenuItem iMonat = new JMenuItem("Monat");
			iMonat.setMnemonic('M');
			iMonat.setActionCommand("Monat");
			iMonat.setEnabled(false);
			iMonat.addActionListener(this);
			mAnsehen.add(iMonat);

			JMenuItem iJahr = new JMenuItem("Jahr");
			iJahr.setMnemonic('J');
			iJahr.setActionCommand("Jahr");
			iJahr.setEnabled(false);
			iJahr.addActionListener(this);
			mAnsehen.add(iJahr);

		JMenu mVerwalten = new JMenu("Verwalten");
		mVerwalten.setMnemonic('V');
		mbar.add(mVerwalten);

			JMenuItem iPerson = new JMenuItem("Pers?nliche Daten");
			iPerson.setMnemonic('P');
			iPerson.setActionCommand("User");
			iPerson.setEnabled(false);
			iPerson.addActionListener(this);
			mVerwalten.add(iPerson);

			JMenuItem iTypen = new JMenuItem("Eintragstypen");
			iTypen.setMnemonic('E');
			iTypen.setActionCommand("Typen");
			iTypen.setEnabled(false);
			iTypen.addActionListener(this);
			mVerwalten.add(iTypen);

			JMenuItem iGruppen = new JMenuItem("Gruppen");
			iGruppen.setMnemonic('G');
			iGruppen.setActionCommand("Gruppen");
			iGruppen.setEnabled(false);
			iGruppen.addActionListener(this);
			mVerwalten.add(iGruppen);

			JMenuItem iRechte = new JMenuItem("Berechtigungen");
			iRechte.setMnemonic('B');
			iRechte.setActionCommand("Rechte");
			iRechte.setEnabled(false);
			iRechte.addActionListener(this);
			mVerwalten.add(iRechte);

		JMenu mInfo = new JMenu("Info");
		mInfo.setMnemonic('I');
		mbar.add(mInfo);

			JMenuItem iRechtsInfo = new JMenuItem("Erhaltene Rechte");
			iRechtsInfo.setMnemonic('E');
			iRechtsInfo.setActionCommand("RechtsInfo");
			iRechtsInfo.setEnabled(false);
			iRechtsInfo.addActionListener(this);
			mInfo.add(iRechtsInfo);

			JMenuItem iNachrichten = new JMenuItem("Nachrichten");
			iNachrichten.setMnemonic('N');
			iNachrichten.setActionCommand("Nachrichten");
			iNachrichten.setEnabled(false);
			iNachrichten.addActionListener(this);
			mInfo.add(iNachrichten);

			mInfo.addSeparator();

			JMenuItem iInfo = new JMenuItem("Info...");
			iInfo.setMnemonic('o');
			iInfo.setActionCommand("About");
			iInfo.setEnabled(false);
			iInfo.addActionListener(this);
			mInfo.add(iInfo);
			
		JMenu mExit = new JMenu("Beenden");
		mExit.setMnemonic('B');
		mbar.add(mExit);

			JMenuItem iExit = new JMenuItem("Beenden");
			iExit.setMnemonic('b');
			iExit.setActionCommand("Exit");
			iExit.addActionListener(this);
			mExit.add(iExit);

			frame.getRootPane().setJMenuBar(mbar);
	}

	protected void createContent()
	{
		JPanel contentPane = (JPanel) frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		layer.setBackground(contentPane.getBackground());
		contentPane.add("Center", layer);

		// DefaultWindow // DefaultWindow // DefaultWindow // DefaultWindow //
		// DefaultWindow
		tagesansicht = new Tagesansicht(frame, statusLabel, this, new Date());
		JInternalFrame gui = tagesansicht.getGUI();
		gui.setVisible(true);
		frames.put("Tag", gui);

/*
		Listener
		listener.addObserver(tagesansicht);
*/

		try
		{
			gui.setSelected(true);
		}
		catch (java.beans.PropertyVetoException ex)
		{ex.printStackTrace();}

		layer.add(gui, 0);
	}
		
	public void actionPerformed(ActionEvent e)
	{
		String c = e.getActionCommand();

		if (e.getSource().getClass() == JMenuItem.class)
		{
			switch (c) {
				case "Termin" -> neuerTermin();

/*
			else if (c.equals("ToDo"))
			{
			}
			else if (c.equals("L?schen"))
			{
			}
*/
				case "Tag" -> tagesAnsicht();

/*
			else if (c.equals("Woche"))
			{
			}
			else if (c.equals("Monat"))
			{
			}
			else if (c.equals("Jahr"))
			{
			}
*/
				case "Exit" -> {
					server.stopServer();
					System.exit(0);
				}
			}
		}

		if (c.equals("Tagesansicht"))			// neuer Termin erzeugt oder Termin geaendert
		{												// deshalb Tagesansicht aktualisieren
			tagesansicht.updateEintraege();
		}
	}
	
	protected void neuerTermin()
	{ 
		EditTerminControl termin = new EditTerminControl(frame, this, tagesansicht.bgnAnsicht);
		JInternalFrame gui = termin.getGUI();
		gui.setVisible(true);
		layer.add(gui, 0);
	}

	protected void tagesAnsicht()
	{
		if (!frames.containsKey("Tag"))
		{
			tagesansicht = new Tagesansicht(frame, statusLabel, this, new Date());
			JInternalFrame gui = tagesansicht.getGUI();
			frames.put("Tag", gui);

			// Listener
			// listener.addObserver(tagesansicht);
		}

		JInternalFrame gui = frames.get("Tag");
		gui.setVisible(true);
		try
		{
			if (gui.isIcon())
				gui.setIcon(false);
			gui.setSelected(true);
		}
		catch (java.beans.PropertyVetoException ex)
		{ ex.printStackTrace();}

		layer.add(gui, 0);
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	class WindowEventHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			server.stopServer();
			System.exit(0);
		}
	}
}
