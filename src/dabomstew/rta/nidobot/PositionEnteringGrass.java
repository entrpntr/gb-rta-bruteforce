package dabomstew.rta.nidobot;

import java.nio.ByteBuffer;

public class PositionEnteringGrass {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((savedState == null) ? 0 : savedState.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PositionEnteringGrass other = (PositionEnteringGrass) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (savedState == null) {
			if (other.savedState != null)
				return false;
		} else if (!savedState.equals(other.savedState))
			return false;
		return true;
	}
	public PositionEnteringGrass(ByteBuffer savedState, String path, String rngState) {
		super();
		this.savedState = savedState;
		this.path = path;
		this.rngState = rngState;
	}
	public ByteBuffer savedState;
	public String path;
	public String rngState;
}
