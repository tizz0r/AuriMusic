package de.timschubert.uwumusic.shared.exceptions;

public class SetupNotFinishedException extends RuntimeException
{
    public SetupNotFinishedException(String message)
    {
        super(message);
    }
}
