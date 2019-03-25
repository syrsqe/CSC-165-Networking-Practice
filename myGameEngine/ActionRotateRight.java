package myGameEngine;

import a1.*;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import ray.rml.Degreef;
public class ActionRotateRight extends AbstractInputAction{
    private Camera camera;
    private SceneNode node;
    private float frameTime;

    public ActionRotateRight(Camera c, SceneNode n){
        camera = c;
        node = n;
    }

    public void performAction(float time, Event event){
        frameTime = (MyGame.getFrameTime()/60f);
        if (camera.getMode() == 'c'){
            Vector3f n = camera.getFd();
            Vector3f v = camera.getUp();
            Vector3f u = camera.getRt();
            Vector3f p = camera.getPo();


            Vector3f newU = (Vector3f) u.rotate(Degreef.createFrom(-2*frameTime), v);
            Vector3f newN = (Vector3f) n.rotate(Degreef.createFrom(-2*frameTime), v);
            camera.setRt(newU);
            camera.setFd(newN);

        }

        if(camera.getMode()== 'r'){
            node.yaw((Degreef.createFrom(-3*frameTime)));
        }


        //camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
    }

}