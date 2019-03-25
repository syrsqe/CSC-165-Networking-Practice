package myGameEngine;
import a1.*;
import java.lang.*;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.*;
import ray.rml.Degreef;

//import ray.rml.Matrix4f;
public class ActionMoveLeft extends AbstractInputAction{
    private Camera camera;
    private SceneNode node;
    private Vector3f subPos;
    private float posVector;
    private static boolean barrier;
    private float frameTime;

    public ActionMoveLeft(Camera c, SceneNode n){
        camera = c;
        node = n;
        barrier = false;
        frameTime = 0;
    }

    public void performAction(float time, Event event) {
        Component c = event.getComponent();
        if(c.getName().equals("A") || event.getValue() < 0){
            frameTime = (MyGame.getFrameTime());
            frameTime = frameTime/500f;
            //System.out.println("elapsed time" + frameTime);
            if (camera.getMode() == 'c') {

                Vector3f u = camera.getRt();
                Vector3f p = camera.getPo();
                Vector3f p1 = (Vector3f) Vector3f.createFrom(-frameTime * u.x(), -frameTime * u.y(), -frameTime * u.z());
                Vector3f p2 = (Vector3f) p.add((Vector3) p1);
                ActionMoveRight.setBarrier(false);


                if(-MyGame.getDolphinCameraDistance() > -1.5f && barrier == false){

                    camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));

                }else if(-MyGame.getDolphinCameraDistance() < -1.5f && barrier == false) {
                    p1 = (Vector3f) Vector3f.createFrom(2*frameTime * u.x(), 2*frameTime * u.y(), 2*frameTime * u.z());
                    p2 = (Vector3f) p.add((Vector3) p1);
                    camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
                    barrier = true;
                }
            } else if (camera.getMode() == 'r') {

                node.moveRight(2*frameTime);

            }
        }

        if(event.getValue() < 0){

        }

    }
    public static void setBarrier(boolean bol){
        barrier = bol;
    }
}