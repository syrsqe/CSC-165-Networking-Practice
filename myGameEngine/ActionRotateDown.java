package myGameEngine;
import a1.*;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import ray.rml.Degreef;
public class ActionRotateDown extends AbstractInputAction{
    private Camera camera;
    private SceneNode node;
    private float frameTime;

    public ActionRotateDown(Camera c, SceneNode n){
        camera = c;
        node = n;
        frameTime = 0;
    }

    public void performAction(float time, Event event){
        frameTime = (MyGame.getFrameTime()/60f);
        if(camera.getMode() == 'c'){
            Vector3f n = camera.getFd();
            Vector3f v = camera.getUp();
            Vector3f u = camera.getRt();
            Vector3f p = camera.getPo();


            Vector3f newV = (Vector3f) v.rotate(Degreef.createFrom(-2*frameTime), u);
            Vector3f newN = (Vector3f) n.rotate(Degreef.createFrom(-2*frameTime), u);
            camera.setUp(newV);
            camera.setFd(newN);

        }
        if(camera.getMode() == 'r'){
            node.pitch((Degreef.createFrom(3*frameTime)));
        }



    }

}