<?php

namespace App\EventSubscriber;

use Symfony\Component\EventDispatcher\Attribute\AsEventListener;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Event\ExceptionEvent;
use Symfony\Component\HttpKernel\Exception\HttpExceptionInterface;
use Symfony\Component\HttpKernel\KernelEvents;
use Symfony\Component\Security\Core\Exception\AccessDeniedException;
use Symfony\Component\Security\Core\Exception\AuthenticationException;

/**
 * Réponses JSON sur /api (Postman / Angular) au lieu d’une page HTML 500 opaque.
 */
final class ApiJsonErrorSubscriber
{
    public function __construct(private readonly bool $debug) {}

    #[AsEventListener(event: KernelEvents::EXCEPTION, priority: -254)]
    public function onKernelException(ExceptionEvent $event): void
    {
        $request = $event->getRequest();
        if (!str_starts_with($request->getPathInfo(), '/api')) {
            return;
        }

        $e = $event->getThrowable();
        $status = $this->resolveStatus($e);
        $headers = $e instanceof HttpExceptionInterface ? $e->getHeaders() : [];

        $payload = [
            'error' => $e->getMessage(),
            'exception' => $e::class,
        ];
        if ($this->debug) {
            $payload['file'] = $e->getFile();
            $payload['line'] = $e->getLine();
            if ($e->getPrevious() instanceof \Throwable) {
                $payload['previous'] = $e->getPrevious()->getMessage();
            }
        }

        $event->setResponse(new JsonResponse($payload, $status, $headers));
    }

    private function resolveStatus(\Throwable $e): int
    {
        if ($e instanceof HttpExceptionInterface) {
            $status = $e->getStatusCode();
        } elseif ($e instanceof AccessDeniedException) {
            $status = Response::HTTP_FORBIDDEN;
        } elseif ($e instanceof AuthenticationException) {
            $status = Response::HTTP_UNAUTHORIZED;
        } else {
            $status = Response::HTTP_INTERNAL_SERVER_ERROR;
        }

        if ($status < 400 || $status > 599) {
            $status = Response::HTTP_INTERNAL_SERVER_ERROR;
        }

        return $status;
    }
}
