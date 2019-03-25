package myGameEngine;
import a1.*;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.*;
import ray.rml.Degreef;
public class ActionRotateVerticle extends AbstractInputAction{
    private Camera camera;
    private SceneNode node;
    private float frameTime;

    public ActionRotateVerticle(Camera c, SceneNode n){
        camera = c;
        node = n;
        frameTime = 0;
    }

    public void performAction(float time, Event event){
        frameTime = (MyGame.getFrameTime()/60f);
        Component c = event.getComponent();
        Vector3f n = camera.getFd();
        Vector3f v = camera.getUp();
        Vector3f u = camera.getRt();
        Vector3f p = camera.getPo();
        if(c.getName().equals("UP") || (event.getValue() < -0.1)) {
            if(camera.getMode() == 'c'){

                Vector3f newV = (Vector3f) v.rotate(Degreef.createFrom(2*frameTime), u);
                Vector3f newN = (Vector3f) n.rotate(Degreef.createFrom(2*frameTime), u);
                camera.setUp(newV);
                camera.setFd(newN);
            }
            if(camera.getMode() == 'r'){
                if(camera.getMode()== 'r'){
                    node.pitch((Degreef.createFrom(-3*frameTime)));
                }
            }
        }
        if(c.getName().equals("DOWN") || (event.getValue() > 0.1)) {
            if(camera.getMode() == 'c'){

                Vector3f newV = (Vector3f) v.rotate(Degreef.createFrom(-2*frameTime), u);
                Vector3f newN = (Vector3f) n.rotate(Degreef.createFrom(-2*frameTime), u);
                camera.setUp(newV);
                camera.setFd(newN);
            }
            if(camera.getMode() == 'r'){
                if(camera.getMode()== 'r'){
                    node.pitch((Degreef.createFrom(3*frameTime)));
                }
            }
        }




        //camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
    }

}