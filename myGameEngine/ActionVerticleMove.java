package myGameEngine;
import a1.*;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.*;

public class ActionVerticleMove extends AbstractInputAction{
    private Camera camera;
    private SceneNode node;
    private static boolean forwardBarrier, backwardBarrier;
    private float frameTime;

    public ActionVerticleMove(Camera c, SceneNode n){
        camera = c;
        node = n;
        frameTime = 0;
        forwardBarrier = false;
        backwardBarrier = false;

    }

    public void performAction(float time, Event event){

        Vector3f n = camera.getFd();
        Vector3f p = camera.getPo();

        frameTime = (MyGame.getFrameTime()/500f);

        Component c = event.getComponent();
        System.out.println(event.getValue());
        if(c.getName().equals("S") || (event.getValue() > 0.1)){
            Vector3f p1 = (Vector3f) Vector3f.createFrom(-frameTime*n.x(), -frameTime*n.y(), -frameTime*n.z());
            Vector3f p2 = (Vector3f) p.add((Vector3)p1);
            if(camera.getMode() == 'c'){

                forwardBarrier = false;


                if((-MyGame.getDolphinCameraDistance()) > -1.5f && backwardBarrier == false){
                    camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
                }else if(MyGame.getDolphinCameraDistance() > 1.5f && backwardBarrier == false)  {
                    p1 = (Vector3f) Vector3f.createFrom(2*frameTime * n.x(), 2*frameTime * n.y(), 2*frameTime * n.z());
                    p2 = (Vector3f) p.add((Vector3) p1);
                    camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
                    backwardBarrier = true;
                }


            }else if (camera.getMode() == 'r') {
                node.moveForward(-2*frameTime);
            }
        }
        if(c.getName().equals("W") || (event.getValue() < -0.1)){
            Vector3f p1 = (Vector3f) Vector3f.createFrom(frameTime*n.x(), frameTime*n.y(), frameTime*n.z());
            Vector3f p2 = (Vector3f) p.add((Vector3)p1);
        if (camera.getMode() == 'c') {

            backwardBarrier = false;

            if(MyGame.getDolphinCameraDistance() < 1.5f && forwardBarrier == false){
                camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
            }else if(MyGame.getDolphinCameraDistance() > 1.5f && forwardBarrier == false)  {
                p1 = (Vector3f) Vector3f.createFrom(-2*frameTime * n.x(), -frameTime * n.y(), -frameTime * n.z());
                p2 = (Vector3f) p.add((Vector3) p1);
                camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
                forwardBarrier = true;
            }


        } else if (camera.getMode() == 'r') {
            node.moveForward(2*frameTime);
        }

    }






    }
    public static void setBarrier(boolean bol){
        forwardBarrier= bol;
    }

}