package myGameEngine;

import a1.*;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class ActionMoveBackward extends AbstractInputAction{
    private Camera camera;
    private SceneNode node;
    private static boolean barrier;
    private float frameTime;

    public ActionMoveBackward(Camera c, SceneNode n){
        camera = c;
        node = n;
        barrier = false;
        frameTime = 0;

    }

    public void performAction(float time, Event event){
        frameTime = (MyGame.getFrameTime()/500f);
        if(camera.getMode() == 'c'){
            Vector3f n = camera.getFd();
            Vector3f p = camera.getPo();
            Vector3f p1 = (Vector3f) Vector3f.createFrom(-frameTime*n.x(), -frameTime*n.y(), -frameTime*n.z());
            Vector3f p2 = (Vector3f) p.add((Vector3)p1);

            ActionMoveForward.setBarrier(false);




            if((-MyGame.getDolphinCameraDistance()) > -1.5f && barrier == false){
                camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
            }else if(MyGame.getDolphinCameraDistance() > 1.5f && barrier == false)  {
                p1 = (Vector3f) Vector3f.createFrom(2*frameTime * n.x(), 2*frameTime * n.y(), 2*frameTime * n.z());
                p2 = (Vector3f) p.add((Vector3) p1);
                camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
                setBarrier(true);
            }


        }else if (camera.getMode() == 'r') {
            node.moveForward(-2*frameTime);
        }





    }
    public static void setBarrier(boolean bol){
        barrier = bol;
    }

}