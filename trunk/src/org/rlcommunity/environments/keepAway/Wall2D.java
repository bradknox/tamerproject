package org.rlcommunity.environments.keepAway;

import java.util.StringTokenizer;

import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;
//This is Google's cached copy of Iso RPG Source/Common/2D/Wall2D.h from
//http://h1.ripway.com/MrAwesome/IsoRPGSource.zip 
public class Wall2D {
Vector2D m_To;
Vector2D m_From;

	public Wall2D(Vector2D from, Vector2D to) {
		m_From=from;
		m_To=to;
		
}

	public Wall2D(String nextWallString) {
		StringTokenizer strTok=new StringTokenizer(nextWallString,"_");
		m_From=new Vector2D(strTok);
		m_To=new Vector2D(strTok);
	}

	public Vector2D Normal() {
		
		 Vector2D temp = (m_To.subtractToCopy(m_From)).normalize();
		 Vector2D theNormal=new Vector2D(-temp.y,temp.x);
		return theNormal;
	}
	
	public Vector2D From(){
		return m_From;
	}
	public Vector2D To(){
		return m_To;
	}

	public String stringSerialize() {
		return m_From.stringSerialize()+"_"+m_To.stringSerialize();
	}

}
