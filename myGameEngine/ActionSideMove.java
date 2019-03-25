package myGameEngine;
import a1.*;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.*;
import ray.rml.Degreef;
//import ray.rml.Matrix4f;
public class ActionSideMove extends AbstractInputAction{
    private Camera camera;
    private SceneNode node;
    private boolean hitedge;
    private static boolean leftBarrier, rightBarrier;
    private float frameTime;

    public ActionSideMove(Camera c, SceneNode n){
        camera = c;
        node = n;
        leftBarrier = false;
        rightBarrier = false;
        frameTime = 0;

    }

    public void performAction(float time, Event event){
        Component c = event.getComponent();
        //make sure either D is pressed or right value of joystick is pressed
        Vector3f u = camera.getRt();
        Vector3f p = camera.getPo();

        frameTime = (MyGame.getFrameTime()/500f);
        System.out.println(event.getValue());
        if(c.getName().equals("D") || (event.getValue() > 0.1)){
            Vector3f p1 = (Vector3f) Vector3f.createFrom(frameTime*u.x(), frameTime*u.y(), frameTime*u.z());
            Vector3f p2 = (Vector3f) p.add((Vector3)p1);
            if(camera.getMode() == 'c'){

                leftBarrier = false;

                if(MyGame.getDolphinCameraDistance() < 1.5f && rightBarrier == false){

                    camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
                }else if(MyGame.getDolphinCameraDistance() > 1.5f && rightBarrier == false) {

                    p1 = (Vector3f) Vector3f.createFrom(-2*frameTime * u.x(), -2*frameTime * u.y(), -2*frameTime * u.z());
                    p2 = (Vector3f) p.add((Vector3) p1);
                    camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
                    rightBarrier = true;

                }

            }else if(camera.getMode() == 'r'){
                node.moveRight(-2*frameTime);
            }
        }
        if(c.getName().equals("A") || event.getValue() < -0.1){
            Vector3f p1 = (Vector3f) Vector3f.createFrom(-frameTime*u.x(), -frameTime*u.y(), -frameTime*u.z());
            Vector3f p2 = (Vector3f) p.add((Vector3)p1);
            frameTime = (MyGame.getFrameTime());
            frameTime = frameTime/500f;
            //System.out.println("elapsed time" + frameTime);
            if (camera.getMode() == 'c') {

                rightBarrier = false;



                if(-MyGame.getDolphinCameraDistance() > -1.5f && leftBarrier == false){

                    camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));

                }else if(-MyGame.getDolphinCameraDistance() < -1.5f && leftBarrier == false) {
                    p1 = (Vector3f) Vector3f.createFrom(2*frameTime * u.x(), 2*frameTime * u.y(), 2*frameTime * u.z());
                    p2 = (Vector3f) p.add((Vector3) p1);
                    camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
                    leftBarrier = true;
                }
            } else if (camera.getMode() == 'r') {

                node.moveRight(2*frameTime);

            }
        }


    }
    public static void setBarrier(boolean bol){
        leftBarrier = bol;
    }

}