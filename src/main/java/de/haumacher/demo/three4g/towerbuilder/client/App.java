package de.haumacher.demo.three4g.towerbuilder.client;

import org.treblereel.gwt.three4g.cameras.PerspectiveCamera;
import org.treblereel.gwt.three4g.core.Object3D;
import org.treblereel.gwt.three4g.geometries.BoxGeometry;
import org.treblereel.gwt.three4g.lights.AmbientLight;
import org.treblereel.gwt.three4g.lights.DirectionalLight;
import org.treblereel.gwt.three4g.materials.MeshPhongMaterial;
import org.treblereel.gwt.three4g.materials.parameters.MeshPhongMaterialParameters;
import org.treblereel.gwt.three4g.math.Color;
import org.treblereel.gwt.three4g.math.Vector3;
import org.treblereel.gwt.three4g.objects.Mesh;
import org.treblereel.gwt.three4g.renderers.WebGLRenderer;
import org.treblereel.gwt.three4g.renderers.parameters.WebGLRendererParameters;
import org.treblereel.gwt.three4g.scenes.Scene;

import com.google.gwt.core.client.EntryPoint;

import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLCanvasElement;

/**
 * {@link EntryPoint} to the hello cube app.
 * 
 * <p>
 * Straight forward translation of the rotating cube example from
 * https://threejsfundamentals.org/threejs/lessons/threejs-fundamentals.html to
 * GWT using the three4g API.
 * </p>
 * 
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class App implements EntryPoint {
	private Scene _scene;

	private Mesh _cube;

	private PerspectiveCamera _camera;

	private WebGLRenderer _renderer;

	private Tower _towerModel = new Tower();
	
	private Direction _direction = Direction.BACK_TO_FRONT;

	private Object3D _tower;

	private boolean _stop;

	private float _hue = 0.0f;

	private Mesh _fragment;

	private double _currentTime;

	private double _fragmentTimeOffset;

	private float _fragmentSpeed;

	private float _fragmentBottom;

	private Direction _fragmentDirection;
	
	static final float BOX_DEPTH = 0.5f;

	private static final float MIN_FRAGMENT = 0.1f;

	@Override
	public void onModuleLoad() {
		HTMLCanvasElement canvas = (HTMLCanvasElement) DomGlobal.document.getElementById("canvas");
		DomGlobal.document.addEventListener("keydown", this::onKeyDown);

		WebGLRendererParameters rendererParams = new WebGLRendererParameters();
		rendererParams.canvas = canvas;
		_renderer = new WebGLRenderer(rendererParams);

		// Create camera.
		float fov = 75;
		float aspect = 1.333f;
		float near = 2f;
		float far = 23;
		_camera = new PerspectiveCamera(fov, aspect, near, far);
		_camera.position.set(5, -8, 5);
		_camera.rotateOnWorldAxis(v(1,0,0), (float)((Math.PI / 2) -Math.atan2(5, 8)));
		_camera.rotateOnWorldAxis(v(0,0,1), (float) Math.atan2(5, 8));
		_camera.updateProjectionMatrix();

		_scene = new Scene();
		
		// Create lights.
		{
			int color = 0xFFFFFF;
			float intensity = 1;
			DirectionalLight light = new DirectionalLight(color, intensity);
			light.position.set(10, 10, 5);
			_scene.add(light);
		}
		
		{
			int color = 0xFFFFFF;
			float intensity = 0.5f;
			AmbientLight light2 = new AmbientLight(color, intensity);
			_scene.add(light2);		
		}

		// Create target platform
		_tower = new Object3D();
		_tower.position.set(0, 0, -BOX_DEPTH);
		_scene.add(_tower);
		
		Platform topModel = new Platform(-3, 3, -3, 3);
		_towerModel.add(topModel);

		BoxGeometry topGeometry = new BoxGeometry(topModel.width(), topModel.height(), BOX_DEPTH);
		Mesh top = new Mesh(topGeometry, createMaterial());
		top.position.set(0, 0, 0);
		_tower.add(top);
		
		createCube();

		// Start the event loop by requesting the first update.
		DomGlobal.requestAnimationFrame(this::startEvent);
	}
	
	Platform top() {
		return _towerModel.top();
	}

	private MeshPhongMaterial createMaterial() {
		MeshPhongMaterialParameters materialParams = new MeshPhongMaterialParameters();
		Color color = new Color();
		color.setHSL(_hue, 0.8f, 0.6f);
		materialParams.color = color;
		MeshPhongMaterial material = new MeshPhongMaterial(materialParams);
		
		_hue += 0.05f;
		if (_hue > 1.0f) {
			_hue -= 1.0f;
		}
		
		return material;
	}

	private void createCube() {
		Platform top = top();
		BoxGeometry boxGeometry = new BoxGeometry(top.width(), top.height(), BOX_DEPTH);
		_cube = new Mesh(boxGeometry, createMaterial());
		if (_direction == Direction.BACK_TO_FRONT) {
			_cube.position.set(top.centerX(), 8, 0);
		} else {
			_cube.position.set(-8, top.centerY(), 0);
		}
		_scene.add(_cube);
	}

	private static Vector3 v(int x, int y, int z) {
		return new Vector3(x, y, z);
	}

	void onKeyDown(Event event) {
		_scene.remove(_cube);
		
		Platform top = top();
		
		float boxWidth = top.width();
		float boxHeight = top.height();
		
		float cubeX1 = _cube.position.x - boxWidth / 2;
		float cubeX2 = _cube.position.x + boxWidth / 2;
		float cubeY1 = _cube.position.y - boxHeight / 2;
		float cubeY2 = _cube.position.y + boxHeight / 2;
		
		Platform match = top.intersect(new Platform(cubeX1, cubeX2, cubeY1, cubeY2));
		
		float matchW = match.width();
		float matchH = match.height();
		
		if (matchW <= 0.0 || matchH <= 0.0) {
			_stop = true;
			return;
		}
		
		float matchX = match.centerX();
		float matchY = match.centerY();
		
		if (_direction == Direction.BACK_TO_FRONT) {
			_fragmentSize = _cube.position.y - top.centerY();
			if (Math.abs(_fragmentSize) < MIN_FRAGMENT) {
				match = top.copy();
				_fragmentSide = null;
			} else {
				float fragmentY;
				if (_fragmentSize > 0) {
					// Behind the tower.
					fragmentY = top.getY2() + _fragmentSize / 2;
					_fragmentSide = Side.BACK;
				} else {
					// In front of the tower.
					fragmentY = top.getY1() + _fragmentSize / 2;
					_fragmentSide = Side.FRONT;
				}
				
				clearFragment();
				BoxGeometry fragmentGeometry = new BoxGeometry(matchW, Math.abs(_fragmentSize), BOX_DEPTH);
				_fragment = new Mesh(fragmentGeometry, _cube.material);
				_fragment.position.set(matchX, fragmentY, BOX_DEPTH * _towerModel.height());
				_fragmentSpeed = 0.0f;
				_fragmentTimeOffset = _currentTime;
				_fragmentDirection = _direction;
				_tower.add(_fragment);
			}
		} else {
			_fragmentSize = _cube.position.x - top.centerX();
			if (Math.abs(_fragmentSize) < MIN_FRAGMENT) {
				match = top.copy();
				_fragmentSide = null;
			} else {
				float fragmentX;
				if (_fragmentSize > 0) {
					// At the right.
					fragmentX = top.getX2() + _fragmentSize / 2;
					_fragmentSide = Side.RIGHT;
				} else {
					// At the left.
					fragmentX = top.getX1() + _fragmentSize / 2;
					_fragmentSide = Side.LEFT;
				}
				
				clearFragment();
				BoxGeometry fragmentGeometry = new BoxGeometry(Math.abs(_fragmentSize), matchH, BOX_DEPTH);
				_fragment = new Mesh(fragmentGeometry, _cube.material);
				_fragment.position.set(fragmentX, matchY, BOX_DEPTH * _towerModel.height());
				_fragmentSpeed = 0.0f;
				_fragmentTimeOffset = _currentTime;
				_fragmentDirection = _direction;
				_tower.add(_fragment);
			}
		}
		
		if (_fragmentSide != null) {
			_fragmentLevel = _towerModel.edgeLevel(_fragmentSide);
			if (_fragmentLevel < 0) {
				_fragmentBottom = -30.0f;
			} else {
				_fragmentBottom = BOX_DEPTH * (_fragmentLevel + 1);
			}
		}
		
		BoxGeometry matchGeometry = new BoxGeometry(matchW, matchH, BOX_DEPTH);
		Mesh matchCube = new Mesh(matchGeometry, _cube.material);
		matchCube.position.set(matchX, matchY, BOX_DEPTH * _towerModel.height());
		_tower.add(matchCube);
		_tower.position.z -= BOX_DEPTH;

		_towerModel.add(match);
		
		_direction = _direction.next();
		createCube();
		_boxTimeOffset += _boxTime;
		_nuberCycles = 0;
	}

	/** 
	 * Drops the currently falling fragment, if any.
	 */
	private void clearFragment() {
		if (_fragment != null) {
			_tower.remove(_fragment);
			_fragment = null;
		}
	}
	
	void startEvent(double timestamp) {
		_currentTime = timestamp;
		
		_boxTimeOffset = timestamp;
		update(timestamp);

		// Request the next update.
		DomGlobal.requestAnimationFrame(this::eventLoop);
	}
	
	void eventLoop(double timestamp) {
		if (_stop) {
			_renderer.render(_scene, _camera);
			return;
		}
		
		update(timestamp);

		// Request the next update.
		DomGlobal.requestAnimationFrame(this::eventLoop);
	}

	// The time in milliseconds when the animation started.
	double _boxTimeOffset = Double.MIN_VALUE;

	// The number of full cycles already elapsed.
	private double _nuberCycles = 0;
	
	// Number of milliseconds for one trip back and forth.
	private static final double PERIOD_MS = 3000;

	private static final float DELTA = 13;
	private static final float DELTA_2 = 2 * DELTA;

	// Acceleration of a falling fragment in units per millisecond^2
	private static final float FRAGMENT_ACCELERATION = 0.0001f;

	private double _boxTime;

	/**
	 * The level the currently falling fragment will break at next.
	 * 
	 * <p>
	 * A value of <code>-1</code> means that the fragment will fall down to negative infinity.
	 * </p>
	 */
	private int _fragmentLevel;

	/**
	 * The {@link Side} of the tower the current fragment is falling down.
	 */
	private Side _fragmentSide;

	private float _fragmentSize;

	void update(double timestamp) {
		_currentTime = timestamp;
		_boxTime = timestamp - _boxTimeOffset;
		
		// A value increasing from 0 to 1. If 1 is reached, starts over at 0.
		double n = (_boxTime / PERIOD_MS) - _nuberCycles;
		if (n > 1.0) {
			n -= 1.0;
			_nuberCycles = _nuberCycles + 1.0;
		}
		
		float deltaY = (float) (DELTA_2 * n);
		if (deltaY > DELTA) {
			deltaY = DELTA_2 - deltaY;
		}
		
		if (_direction == Direction.BACK_TO_FRONT) {
			_cube.position.y = 10 - deltaY;
		} else {
			_cube.position.x = -10 + deltaY;
		}
		
		if (_fragment != null) {
			float deltaT = (float) (timestamp - _fragmentTimeOffset);
			_fragmentSpeed += FRAGMENT_ACCELERATION * deltaT;
			_fragment.position.z -= _fragmentSpeed;
			if (_fragment.position.z < _fragmentBottom) {
				if (_fragmentLevel < 0) {
					// Fragment has fallen to negative infinity.
					clearFragment();
				} else {
					float position = _towerModel.get(_fragmentLevel + 1).position(_fragmentSide);
					float landingPosition = _towerModel.get(_fragmentLevel).position(_fragmentSide);
					
					float landingSize = landingPosition - position;
					float nextFragmentSize = _fragmentSize - landingSize;
					
					if (!_fragmentSide.pointsToOutside(nextFragmentSize)) {
						// Fragment completely fits to the platform it is fallen to.
						_fragment.position.z = BOX_DEPTH * (_fragmentLevel + 1);
						
						// Enlarge the platform on the level where the fragment stopped.
						_towerModel.get(_fragmentLevel + 1).incPosition(_fragmentSide, _fragmentSize);
						
						// Stop fragment animation.
						_fragment = null;
					} else {
						Platform top = top();
						
						// Break fragment into two.
						Mesh landingFragment;
						Mesh nextFragment;
						if (_fragmentDirection == Direction.BACK_TO_FRONT) {
							float width = top.width();
							float centerX = top.centerX();
							BoxGeometry landingGeometry = new BoxGeometry(width, Math.abs(landingSize), BOX_DEPTH);
							landingFragment = new Mesh(landingGeometry, _fragment.material);
							landingFragment.position.set(centerX, position + landingSize / 2, BOX_DEPTH * (_fragmentLevel + 1));
							
							BoxGeometry nextGeometry = new BoxGeometry(width, Math.abs(nextFragmentSize), BOX_DEPTH);
							nextFragment = new Mesh(nextGeometry, _fragment.material);
							nextFragment.position.set(centerX, landingPosition + nextFragmentSize / 2, _fragment.position.z);
						} else {
							float height = top.height();
							float centerY = top.centerY();
							BoxGeometry landingGeometry = new BoxGeometry(Math.abs(landingSize), height, BOX_DEPTH);
							landingFragment = new Mesh(landingGeometry, _fragment.material);
							landingFragment.position.set(position + landingSize / 2, centerY, BOX_DEPTH * (_fragmentLevel + 1));
							
							BoxGeometry nextGeometry = new BoxGeometry(Math.abs(nextFragmentSize), height, BOX_DEPTH);
							nextFragment = new Mesh(nextGeometry, _fragment.material);
							nextFragment.position.set(landingPosition + nextFragmentSize / 2, centerY, _fragment.position.z);
						}

						_tower.add(landingFragment);
						
						_tower.remove(_fragment);
						_fragment = nextFragment;
						_fragmentSize = nextFragmentSize;
						_tower.add(_fragment);
						
						// Enlarge the platform on the level where the fragment stopped.
						_towerModel.get(_fragmentLevel + 1).setPosition(_fragmentSide, landingPosition);
						
						int nextLevel = _towerModel.edgeLevel(_fragmentSide, _fragmentLevel);
						if (nextLevel < 0) {
							_fragmentBottom = -30;
						} else {
							_fragmentBottom = BOX_DEPTH * (nextLevel - 1);
						}
						_fragmentLevel = nextLevel;
					}
				}
			}
		}

		// Render the updated scene.
		_renderer.render(_scene, _camera);
	}

}
