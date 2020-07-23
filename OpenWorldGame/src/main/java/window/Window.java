package window;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import timer.Timer;

public class Window {

	private static String TITLE = "Test Game";
	private static final int WIDTH = 1700;
	private static final int HEIGHT = 955;
	private static final boolean VSYNC = true;
	
	private static long windowID;

	private static Timer timer = new Timer();
	private static float lastFrameTime;
	
	private static double scrollDelta;
	private static DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
	private static DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);
	private static double[] currentFrameMousePos = new double[2];
	private static double[] lastFrameMousePos = new double[2];

	private static float benchTimeElapsed = 0.0f;
	private static float benchmarkTime = 15.0f; // 1 minute
	private static int frameCounter = 0;
	
	public static void createDisplay() {
		
		// Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialise GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE); // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        
        // Create the window
        windowID = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
        if (windowID == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(windowID, (windowID, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(windowID, true); // We will detect this in the rendering loop
            }
        });
        
        glfwSetScrollCallback(windowID, (windowID, dx, dy) -> {
        	scrollDelta = dy;
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Centre our window
        glfwSetWindowPos(
                windowID,
                (vidmode.width() - WIDTH) / 2,
                (vidmode.height() - HEIGHT) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowID);
        // Enable v-sync
        if (VSYNC) {
        	glfwSwapInterval(1);
        }

        // Make the window visible
        glfwShowWindow(windowID);
        
        GL.createCapabilities();
        
        setClearColour(1.0f, 0.0f, 0.0f, 0.0f); 
	
	}
	
	public static void setClearColour(float r, float g, float b, float alpha) {
		glClearColor(r, g, b, alpha);
	}
	
	public static void destroyWindow() {
    	glfwFreeCallbacks(windowID);
        glfwDestroyWindow(windowID);
    }
	
	public static void stopGLFW() {
    	// Terminate GLFW and release the GLFWerrorfun
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
	
	public static boolean isKeyPressed(int keyCode) {
    	return glfwGetKey(windowID, keyCode) == GLFW_PRESS;
    }
	
	public static boolean isMouseButtonPressed(int buttonCode) {
		return glfwGetMouseButton(windowID, buttonCode) == GLFW_PRESS;
	}
	
	public static boolean windowShouldClose() {
    	return glfwWindowShouldClose(windowID);
    }
	
	public static void updateDisplay() {
		
		glfwSwapBuffers(windowID);
		glfwPollEvents();
		// Measures the time taken for the last frame.
		lastFrameTime = timer.getElapsedTime();
	}
	
	/*
	 * Returns the time taken to render the last frame, in seconds.
	 */
	public static float getLastFrameTime() {
		return lastFrameTime;
	}

	public static int getWidth() {
		return WIDTH;
	}

	public static int getHeight() {
		return HEIGHT;
	}
	
	public static double getScrollDelta() {
		return scrollDelta;
	}
	
	/**
	 * This has to be called every frame if we want to detect when a user has stopped moving their mouse.
	 * A callback would only give us information when the cursor moves. This has the result of 
	 * leading to a delta which is always non-zero.
	 */
	public static void updateMousePosition() {
		lastFrameMousePos = currentFrameMousePos.clone();
		
		glfwGetCursorPos(windowID, mouseX, mouseY);

		currentFrameMousePos[0] = mouseX.get();
		currentFrameMousePos[1] = mouseY.get();
		
		mouseX.flip();
		mouseY.flip();
	}
	
	public static double[] getMousePosition() {
		return currentFrameMousePos;
	}
	
	public static double[] getMouseDelta() {
		double[] mouseDelta = new double[2];
		mouseDelta[0] = currentFrameMousePos[0] - lastFrameMousePos[0];
		mouseDelta[1] = currentFrameMousePos[1] - lastFrameMousePos[1];
		return mouseDelta;
	}
	
	/**
	 * Should be called after using scroll delta, otherwise it will
	 * never reset to zero. This is because the callback only occurs when
	 * the scrollwheel is moving, and so will never set the delta to 0.
	 */
	public static void resetScrollDelta() {
		scrollDelta = 0;
	}
	
	public static float getFPS() {
		return (1.0f / getLastFrameTime());
	}
	
	public static boolean benchMark() {
		if (benchTimeElapsed <= benchmarkTime) {
			benchTimeElapsed += getLastFrameTime();
			frameCounter++;
			return false;
		} else {
			System.out.println("Benchmark finished. " + frameCounter + " frames rendered in 30 sec");
			frameCounter = 0;
			benchTimeElapsed = 0;
			return true;
		}
	}
	
}
