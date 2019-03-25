package myGameEngine;
import a1.*;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.*;
import ray.rml.Degreef;
//import ray.rml.Matrix4f;
public class ActionMoveRight extends AbstractInputAction{
    private Camera camera;
    private SceneNode node;
    private boolean hitedge;
    private static boolean barrier;
    private float frameTime;

    public ActionMoveRight(Camera c, SceneNode n){
        camera = c;
        node = n;
        barrier = false;
        frameTime = 0;

    }

    public void performAction(float time, Event event){
        Component c = event.getComponent();
        //make sure either D is pressed or right value of joystick is pressed
        System.out.println(event.getValue());
        if(c.getName().equals("D") || (event.getValue() > 0)){
            frameTime = (MyGame.getFrameTime()/500f);
            if(camera.getMode() == 'c'){
                Vector3f u = camera.getRt();
                Vector3f p = camera.getPo();
                Vector3f p1 = (Vector3f) Vector3f.createFrom(frameTime*u.x(), frameTime*u.y(), frameTime*u.z());
                Vector3f p2 = (Vector3f) p.add((Vector3)p1);
                ActionMoveLeft.setBarrier(false);







                if(MyGame.getDolphinCameraDistance() < 1.5f && barrier == false){

                    camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
                }else if(MyGame.getDolphinCameraDistance() > 1.5f && barrier == false) {

                    p1 = (Vector3f) Vector3f.createFrom(-2*frameTime * u.x(), -frameTime * u.y(), -frameTime * u.z());
                    p2 = (Vector3f) p.add((Vector3) p1);
                    camera.setPo((Vector3f) Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
                    setBarrier(true);

                }

            }else if(camera.getMode() == 'r'){
                node.moveRight(-2*frameTime);
            }
        }


    }
    public static void setBarrier(boolean bol){
        barrier = bol;
    }

}