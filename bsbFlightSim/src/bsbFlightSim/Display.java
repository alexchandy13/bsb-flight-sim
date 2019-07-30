package bsbFlightSim;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;


public class Display extends JFrame
implements ActionListener
{
	private JTextField deg, undercut, vpitch, dist;
	private JLabel labeldeg, labelcut, labelpitch;
	private ImageIcon player;

public Display()
{
	super("Launch Path Simulator");
	labeldeg = new JLabel("Attack Angle (Degrees):", SwingConstants.RIGHT);
	deg = new JTextField("10");
	deg.setPreferredSize(new Dimension(80, 30));
	deg.addActionListener(this);

	labelcut = new JLabel("Undercut (Inches):", SwingConstants.RIGHT);
	undercut = new JTextField("0.5");
	undercut.setPreferredSize(new Dimension(80, 30));
	undercut.addActionListener(this);

	labelpitch = new JLabel("Pitch Speed (MPH):", SwingConstants.RIGHT);
	vpitch = new JTextField("95");
	vpitch.setPreferredSize(new Dimension(80, 30));
	vpitch.addActionListener(this);


	dist = new JTextField("Distance Travelled (Feet), Area of Contact (Square Inches), Exit Angle (Degrees), Exit Velocity (MPH)");
	dist.setPreferredSize(new Dimension(300, 30));
	dist.addActionListener(this);
	dist.setEditable(false);
 
	JButton go = new JButton("Compute");
	go.addActionListener(this);

	Container c = getContentPane();
	c.setBackground(Color.WHITE);
	JPanel p = new JPanel();
 
	p.add(labeldeg);
	p.add(deg);
	p.add(labelcut);
	p.add(undercut);
	p.add(labelpitch);
	p.add(vpitch);
	p.add(go);
 
	c.add(p, BorderLayout.SOUTH);
	c.add(dist, BorderLayout.NORTH);
 
}

public void actionPerformed(ActionEvent e)
{
	double degrees = Double.parseDouble(deg.getText());
	double undercutt = Double.parseDouble(undercut.getText());
	double pitchspeed = Double.parseDouble(vpitch.getText());
	//DisplayPanel chart = new DisplayPanel(degrees, undercutt, pitchspeed);
	paintComponent(getGraphics());
	String distance = calcDist(degrees, undercutt, pitchspeed);
	dist.setText(distance);
}

public void paintComponent(Graphics g)
{
	super.paintComponents(g);

	int wi = getWidth();
	int h = getHeight();
	int x = wi/2;
	int y = h/2;
	int r = Math.min(wi, h) / 4;
	drawBallPath(g, x, y, r, Double.parseDouble(deg.getText()), Double.parseDouble(undercut.getText()), Double.parseDouble(vpitch.getText()));
//  drawLegend(g, x, y, r);
}

public void drawBallPath(Graphics g, int x, int y, int r, double degrees, double undercutt, double pitchspeed)
{
//	double degrees = Double.parseDouble(deg.getText());
//	double undercutt = Double.parseDouble(undercut.getText());
//	double pitchspeed = Double.parseDouble(vpitch.getText());
	double oibat = degrees/(180/Math.PI); // Attack Angle of Swing
	double undercut = undercutt/39.37; // Undercut
	double relspeed = -1.0*pitchspeed/2.237; // Speed of Pitch at Release

		
	// Constants
			
	double e = 0.3; // Coefficient of Restitution
	double mball = 0.145; // Mass of Ball
	double mbat = 0.9; // Mass of Bat
	double rball = 0.03734; // Radius of Ball
	double rbat = 0.03302; // Radius of Bat
	double viball = relspeed+5; // Speed of Pitch at Hit
	double relheight = 4/3; // Pitcher Release Height
	double oiball = Math.atan(relheight/18.44); // Attack Angle of Pitch
		
	// Calculation of Exit Velocity and Exit Angle
	
	// Initial Values
	
	double vibat = (-10*oibat)+40; 
	double ttimpact = 1/vibat;
	double dpitch = -viball*ttimpact;
	double xball = dpitch*Math.cos(oiball);
	double yball = dpitch*Math.sin(oiball);
	double xbat = -Math.cos(oibat);
	double ybat = -Math.sin(oibat)-undercut;
	double vixbat = vibat*Math.cos(oibat);
	double viybat = vibat*Math.sin(oibat);
	double vixball = viball*Math.cos(oiball);
	double viyball = viball*Math.sin(oiball);	
	
	// Initialize Variables
	
	double r12,m21,d,gammav,gammaxy,dgamma,dr,dc,sqs,t,
	dvx2,a,x21,y21,vix21,viy21,pi2,vix_cm,viy_cm,alpha;
		
	pi2=2*Math.acos(-1);
	   
	// Find relative radii, mass, position, velocities.
		
    r12=rball+rbat;
    m21=mbat/mball;
    x21=xbat-xball;
    y21=ybat-yball;
    vix21=vixbat-vixball;	
    viy21=viybat-viyball;
	
    // Find Center of Mass of System
	    
	vix_cm = (mball*vixball+mbat*vixbat)/(mball+mbat) ; 
    viy_cm = (mball*viyball+mbat*viybat)/(mball+mbat) ;
	      
    // Find Gamma Values
	    
    gammav=Math.atan2(-viy21,-vix21); 
    d=Math.sqrt(x21*x21 +y21*y21);
    gammaxy=Math.atan2(y21,x21); 
    dgamma=gammaxy-gammav; 
    if (dgamma>pi2) 
    		dgamma=dgamma-pi2;
	else if (dgamma<-pi2) 
		dgamma=dgamma+pi2;
	dr=d*Math.sin(dgamma)/r12; 
    alpha=Math.asin(dr); 
    dc=d*Math.cos(dgamma);
    if (dc>0)
    		sqs=1.0;
    	else 
    		sqs=-1.0;
	t=(dc-sqs*r12*Math.sqrt(1-dr*dr))/Math.sqrt(vix21*vix21+ viy21*viy21); 
	  
	// Find Positions of Ball
	    
    xball=xball+vixball*t; 
    yball=yball+viyball*t; 
    xbat=xbat+vixbat*t; 
	ybat=ybat+viybat*t; 

	a=Math.tan(gammav+alpha);
	dvx2=-2*(vix21 +a*viy21) /((1+a*a)*(1+m21));

	// Update Velocities

	vixbat=vixbat+dvx2; 
	viybat=viybat+a*dvx2;
	vixball=vixball-m21*dvx2;
	viyball=viyball-a*m21*dvx2;

	// Factor in Coefficient of Restitution

	vixball=(vixball-vix_cm)*e + vix_cm; 
	viyball=(viyball-viy_cm)*e + viy_cm;
	vixbat=(vixbat-vix_cm)*e + vix_cm;
	viybat=(viybat-viy_cm)*e + viy_cm;

	oiball = (180/Math.PI)*Math.atan(viyball/vixball);
	oibat = (180/Math.PI)*Math.atan(viybat/vixbat);

		double vfball = Math.sqrt((vixball*vixball)+(viyball*viyball)); // Exit Velocity of Ball
		double vfbat = Math.sqrt((vixbat*vixbat)+(viybat*viybat)); // Exit Velocity of Bat
		
		double w1 = ((1200/.07)*undercut)-175; // Exit Spin Rate of Ball
		double s = rball*w1/vfball; // Spin Factor
		
		// Calculate Lift Coefficient
		
		double cl=0;
		if (s<=0.1) {
			cl = 1.5*s;
		}
		else if (s>0.1) {
			cl = 0.09+0.6*s;
		}
		
		double cd = 0.3; // Drag Coefficient
		
		// Calculate Force Due to Lift
		
		double fm = 0.5*cl*1.225*0.0043*(vfball*vfball); 
		double fmx = -fm*Math.sin(((oiball)/((180/Math.PI))));
		double fmy = fm*Math.cos((oiball)/((180/Math.PI)));
		
		// Calculate Force Due to Drag
		
		double fd = 0.5*cd*1.225*0.0043*(vfball*vfball);
		double fdx = -fd*Math.cos(oiball/((180/Math.PI)));
		double fdy = -fd*Math.sin(oiball/((180/Math.PI)));
		double fg = -9.8*mball; // Force due to Gravity
		
		// Calculate Net Force and Acceleration

		double fnety = fdy+fg+fmy;
		double fnetx = fdx+fmx;
		double accballx = fnetx/mball;
		double accbally = fnety/mball;
		
		// Calculate Distance
		
		double ydt=0;
		double xdt=0;
		double vxdt = 0;
		double vydt = 0;
		double ix=0;
		double iy=2;
		double dt=0.01;
		g.drawLine(x-400, y, x+400, y);
		g.drawLine(x+400, y, x+400, y-20);
		while (iy >= 0) {
			
			vxdt = vixball+(accballx*dt);
			vydt = viyball+(accbally*dt);
			xdt = ix+(vxdt*dt);
			ydt = iy+(vydt*dt);
			double vdt = Math.hypot(vxdt, vydt);
			fd = 0.5*cd*1.225*0.0043*(vdt*vdt);
			fdx = -fd*Math.cos(Math.atan(vydt/vxdt));
			fdy = -fd*Math.sin(Math.atan(vydt/vxdt));
			fm = 0.5*cl*1.225*0.0043*(vdt*vdt);
			fmx = -fm*Math.sin(Math.atan(vydt/vxdt));
			fmy = fm*Math.cos(Math.atan(vydt/vxdt));
			fnety = fdy+fg+fmy;
			fnetx = fdx+fmx;
			accballx = fnetx/mball;
			accbally = fnety/mball;
			vixball = vxdt;
			viyball = vydt;
			ix = xdt;
			iy = ydt;
			int spx = (int) ((x+(3.28*ix)*2)-400);
			int spy = (int) ((y-(3.28*iy)*2));
			int evc = (int) ((vfball-40)*8);
			Color ev = new Color(150+evc,0,150-evc);
			g.setColor(ev);
			
			g.fillOval(spx, spy, 8,8);
			
		}
			int spx = (int) ((x+(3.28*ix)*2)-200);
			int spy = (int) ((y-(3.28*iy)*2));
			int xd = (int) (3.28*ix);
			
			
			
			g.drawString(xd+"", spx-210, spy+25);
			g.setColor(Color.BLACK);
			g.drawString("Assumed Barrel Speed: 90 MPH", 5, 400);
			g.drawString("Assumed Distance to Wall: 400 Feet", 5, 415);
			g.setColor(Color.RED);
			g.drawString("Brighter Red indicates Higher Exit Velo", 5, 430);
			g.setColor(Color.BLUE);
			g.drawString("Brighter Blue indicates Lower Exit Velo", 5, 445);
			g.setFont(new Font("Calibri", Font.PLAIN, 36));
			
			if (ix>=124.92) {
				g.setColor(Color.GREEN);
				g.drawString("DINGER!", spx-700, spy-200);
			} else if (oiball<-5) {
				g.setColor(Color.RED);
				g.drawString("Weak grounder", spx-700, spy-200);
			} else if (oiball>50) {
				g.setColor(Color.RED);
				g.drawString("Can of Corn", spx-700, spy-200);
			}
			
			
		
}

private String calcDist(double degrees,  double undercutt, double pitchspeed)
{
		// Independent Variables
		//double degrees = 0;
		double oibat = degrees/(180/Math.PI); // Attack Angle of Swing
		double undercut = undercutt/39.37; // Undercut
		double relspeed = -1.0*pitchspeed/2.237; // Speed of Pitch at Release
	
		
		// Constants
			
		double e = 0.3; // Coefficient of Restitution
		double mball = 0.145; // Mass of Ball
		double mbat = 0.9; // Mass of Bat
		double rball = 0.03734; // Radius of Ball
		double rbat = 0.03302; // Radius of Bat
		double viball = relspeed+5; // Speed of Pitch at Hit
		double relheight = 4/3; // Pitcher Release Height
		double oiball = Math.atan(relheight/18.44); // Attack Angle of Pitch
		
		// Calculation of Area of Contact
		
		double dibat = (rbat*2);
		double parah = dibat;
		double diball = rball*2;
		double parab =0;
		if (oiball > oibat) {
		parab = diball/Math.sin(oiball-oibat);
		}
		else if (oibat >= oiball) {
			parab = diball/Math.sin(oibat-oiball);
		}

		double area = (parah*parab); // Area of Contact
		
		// Calculation of Exit Velocity and Exit Angle
		
		// Initial Values
		
		double vibat = (-10*oibat)+40; 
		double ttimpact = 1/vibat;
		double dpitch = -viball*ttimpact;
		double xball = dpitch*Math.cos(oiball);
		double yball = dpitch*Math.sin(oiball);
		double xbat = -Math.cos(oibat);
		double ybat = -Math.sin(oibat)-undercut;
		double vixbat = vibat*Math.cos(oibat);
		double viybat = vibat*Math.sin(oibat);
		double vixball = viball*Math.cos(oiball);
		double viyball = viball*Math.sin(oiball);	
		
		// Initialize Variables
		
		double r12,m21,d,gammav,gammaxy,dgamma,dr,dc,sqs,t,
    dvx2,a,x21,y21,vix21,viy21,pi2,vix_cm,viy_cm,alpha;
		
		pi2=2*Math.acos(-1);
	   
		// Find relative radii, mass, position, velocities.
		
	    r12=rball+rbat;
	    m21=mbat/mball;
	    x21=xbat-xball;
	    y21=ybat-yball;
	    vix21=vixbat-vixball;	
	    viy21=viybat-viyball;

	    // Find Center of Mass of System
	    
	    vix_cm = (mball*vixball+mbat*vixbat)/(mball+mbat) ; 
	    viy_cm = (mball*viyball+mbat*viybat)/(mball+mbat) ;
	      
	    // Find Gamma Values
	    
	    gammav=Math.atan2(-viy21,-vix21); 
	    d=Math.sqrt(x21*x21 +y21*y21);
	    gammaxy=Math.atan2(y21,x21); 
	    dgamma=gammaxy-gammav; 
	    if (dgamma>pi2) {dgamma=dgamma-pi2;}
    else if (dgamma<-pi2) {dgamma=dgamma+pi2;}
	    dr=d*Math.sin(dgamma)/r12; 
	    alpha=Math.asin(dr); 
	    dc=d*Math.cos(dgamma);
	    if (dc>0) {sqs=1.0;} else {sqs=-1.0;}
	    t=(dc-sqs*r12*Math.sqrt(1-dr*dr))/Math.sqrt(vix21*vix21+ viy21*viy21); 
	  
	    // Find Positions of Ball
	    
	    xball=xball+vixball*t; 
	    yball=yball+viyball*t; 
	    xbat=xbat+vixbat*t; 
    ybat=ybat+viybat*t; 
    
    a=Math.tan(gammav+alpha);
    dvx2=-2*(vix21 +a*viy21) /((1+a*a)*(1+m21));
    
    // Update Velocities
    
    vixbat=vixbat+dvx2; 
    viybat=viybat+a*dvx2;
    vixball=vixball-m21*dvx2;
    viyball=viyball-a*m21*dvx2;
    
    // Factor in Coefficient of Restitution
    
    vixball=(vixball-vix_cm)*e + vix_cm; 
    viyball=(viyball-viy_cm)*e + viy_cm;
    vixbat=(vixbat-vix_cm)*e + vix_cm;
    viybat=(viybat-viy_cm)*e + viy_cm;
    
    oiball = (180/Math.PI)*Math.atan(viyball/vixball);
    oibat = (180/Math.PI)*Math.atan(viybat/vixbat);
    
		double vfball = Math.sqrt((vixball*vixball)+(viyball*viyball)); // Exit Velocity of Ball
		double vfbat = Math.sqrt((vixbat*vixbat)+(viybat*viybat)); // Exit Velocity of Bat
		
		double w = ((1200/.07)*undercut)-175; // Exit Spin Rate of Ball
		double s = rball*w/vfball; // Spin Factor
		
		// Calculate Lift Coefficient
		
		double cl=0;
		if (s<=0.1) {
			cl = 1.5*s;
		}
		else if (s>0.1) {
			cl = 0.09+0.6*s;
		}
		
		double cd = 0.3; // Drag Coefficient
		
		// Calculate Force Due to Lift
		
		double fm = 0.5*cl*1.225*0.0043*(vfball*vfball); 
		double fmx = -fm*Math.sin(((oiball)/((180/Math.PI))));
		double fmy = fm*Math.cos((oiball)/((180/Math.PI)));
		
		// Calculate Force Due to Drag
		
		double fd = 0.5*cd*1.225*0.0043*(vfball*vfball);
		double fdx = -fd*Math.cos(oiball/((180/Math.PI)));
		double fdy = -fd*Math.sin(oiball/((180/Math.PI)));
		double fg = -9.8*mball; // Force due to Gravity
		
		// Calculate Net Force and Acceleration

		double fnety = fdy+fg+fmy;
		double fnetx = fdx+fmx;
		double accballx = fnetx/mball;
		double accbally = fnety/mball;
		
		// Calculate Distance
		
		double ydt=0;
		double xdt=0;
		double vxdt = 0;
		double vydt = 0;
		double ix=0;
		double iy=2;
		double dt=0.01;
		while (iy >= 0) {
			
			vxdt = vixball+(accballx*dt);
			vydt = viyball+(accbally*dt);
			xdt = ix+(vxdt*dt);
			ydt = iy+(vydt*dt);
			double vdt = Math.hypot(vxdt, vydt);
			fd = 0.5*cd*1.225*0.0043*(vdt*vdt);
			fdx = -fd*Math.cos(Math.atan(vydt/vxdt));
			fdy = -fd*Math.sin(Math.atan(vydt/vxdt));
			fm = 0.5*cl*1.225*0.0043*(vdt*vdt);
			fmx = -fm*Math.sin(Math.atan(vydt/vxdt));
			fmy = fm*Math.cos(Math.atan(vydt/vxdt));
			fnety = fdy+fg+fmy;
			fnetx = fdx+fmx;
			accballx = fnetx/mball;
			accbally = fnety/mball;
			vixball = vxdt;
			viyball = vydt;
			ix = xdt;
			iy = ydt;
			
		}
		ix = 3.28*ix;
		int xft = (int) ix;
		area = 61023.7*area;
		int areai = (int) area;
		int exitangle = (int) oiball;
		vfball = 2.237*vfball;
		int exitvelo = (int) vfball;
		
	return "Distance Travelled (Feet): "+String.valueOf(xft)+"  "+" Area of Contact (Square Inches): "+String.valueOf(areai)
			+"  "+" Exit Angle (Degrees): "+String.valueOf(exitangle)+"  "+" Exit Velocity (MPH): "+String.valueOf(exitvelo);
}




public static void main(String[] args)
{
	Display w = new Display();
	w.setBounds(300, 300, 1000, 700);
	w.setDefaultCloseOperation(EXIT_ON_CLOSE);
	w.setVisible(true);
}

}

