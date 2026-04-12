<?php

namespace App\Controller;

use App\Entity\Collaboration;
use App\Repository\CollaborationRepository;
use App\Security\KeycloakUser;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\BinaryFileResponse;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\ResponseHeaderBag;
use Symfony\Component\DependencyInjection\Attribute\Autowire;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/api/collaborations')]
class CollaborationController extends AbstractController
{
    private const ALLOWED_IMAGE = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

    /** MIME détectés ; les .mp4 sont souvent signalés application/octet-stream sous Windows — voir effectiveMimeForUpload(). */
    private const ALLOWED_VIDEO = [
        'video/mp4', 'video/webm', 'video/quicktime',
        'application/mp4', 'video/x-m4v',
    ];

    /** @var array<string, string> */
    private const VIDEO_EXT_TO_MIME = [
        'mp4' => 'video/mp4',
        'm4v' => 'video/mp4',
        'webm' => 'video/webm',
        'mov' => 'video/quicktime',
        'qt' => 'video/quicktime',
    ];

    /** @var array<string, string> */
    private const IMAGE_EXT_TO_MIME = [
        'jpg' => 'image/jpeg',
        'jpeg' => 'image/jpeg',
        'png' => 'image/png',
        'webp' => 'image/webp',
        'gif' => 'image/gif',
    ];

    public function __construct(
        #[Autowire('%kernel.project_dir%')]
        private readonly string $projectDir,
    ) {
    }

    private function uploadDir(): string
    {
        $dir = $this->projectDir.'/var/collaboration_uploads';
        if (!is_dir($dir)) {
            mkdir($dir, 0775, true);
        }

        return $dir;
    }

    /**
     * Détection MIME fiable pour upload : fileinfo + repli sur l’extension si octet-stream / inconnu.
     *
     * @return non-empty-string|null
     */
    private function effectiveMimeForUpload(UploadedFile $file, string $type): ?string
    {
        $mime = '';
        try {
            $mime = trim((string) $file->getMimeType());
        } catch (\Throwable) {
        }
        if ('' === $mime) {
            $mime = trim((string) $file->getClientMimeType());
        }

        $allowed = 'VIDEO' === $type ? self::ALLOWED_VIDEO : self::ALLOWED_IMAGE;
        if (\in_array($mime, $allowed, true)) {
            return $mime;
        }

        $ext = strtolower((string) pathinfo($file->getClientOriginalName(), \PATHINFO_EXTENSION));
        $ext = preg_replace('/[^a-z0-9]/', '', $ext) ?: '';

        if ('VIDEO' === $type && isset(self::VIDEO_EXT_TO_MIME[$ext])) {
            return self::VIDEO_EXT_TO_MIME[$ext];
        }
        if ('IMAGE' === $type && isset(self::IMAGE_EXT_TO_MIME[$ext])) {
            return self::IMAGE_EXT_TO_MIME[$ext];
        }

        return null;
    }

    #[Route('/public', name: 'collaboration_public', methods: ['GET'])]
    public function publicAds(CollaborationRepository $repository): JsonResponse
    {
        $now = new \DateTimeImmutable();
        $all = $repository->findApprovedPublic($now);

        $byPriority = static function (array $list, string $p): array {
            return array_values(array_filter($list, static fn (Collaboration $c) => $c->getPriority() === $p));
        };

        $high = $byPriority($all, 'HIGH');
        $medium = $byPriority($all, 'MEDIUM');
        $low = $byPriority($all, 'LOW');

        return new JsonResponse([
            'high' => array_map(fn (Collaboration $c) => $this->serializePublic($c), $high),
            'medium' => array_map(fn (Collaboration $c) => $this->serializePublic($c), $medium),
            'low' => array_map(fn (Collaboration $c) => $this->serializePublic($c), $low),
            'all' => array_map(fn (Collaboration $c) => $this->serializePublic($c), $all),
        ]);
    }

    #[Route('/media/{id}', name: 'collaboration_media', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function media(Collaboration $collaboration): Response
    {
        if ('APPROVED' !== $collaboration->getStatus() || !$collaboration->isActive()) {
            throw $this->createNotFoundException();
        }

        return $this->streamCollaborationFile($collaboration);
    }

    /** Aperçu admin pour fichiers encore en PENDING. */
    #[Route('/preview/{id}', name: 'collaboration_preview', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function preview(Collaboration $collaboration): Response
    {
        $user = $this->getUser();
        if (!$user instanceof KeycloakUser || !\in_array('ROLE_ADMIN', $user->getRoles(), true)) {
            throw $this->createAccessDeniedException();
        }

        return $this->streamCollaborationFile($collaboration);
    }

    private function streamCollaborationFile(Collaboration $collaboration): Response
    {
        $path = $this->uploadDir().'/'.$collaboration->getFileName();
        if (!is_file($path)) {
            throw $this->createNotFoundException();
        }

        $response = new BinaryFileResponse($path);
        $ext = strtolower((string) pathinfo($collaboration->getFileName(), \PATHINFO_EXTENSION));
        $mime = match ($collaboration->getType()) {
            'VIDEO' => match ($ext) {
                'webm' => 'video/webm',
                'mov', 'qt' => 'video/quicktime',
                default => 'video/mp4',
            },
            default => match ($ext) {
                'png' => 'image/png',
                'gif' => 'image/gif',
                'webp' => 'image/webp',
                default => 'image/jpeg',
            },
        };
        $response->headers->set('Content-Type', $mime);
        $response->setContentDisposition(ResponseHeaderBag::DISPOSITION_INLINE, $collaboration->getFileName());

        return $response;
    }

    #[Route('/mine', name: 'collaboration_mine', methods: ['GET'])]
    public function mine(CollaborationRepository $repository): JsonResponse
    {
        $user = $this->getKeycloakUser();
        $list = $repository->findByUserId($user->getSubject());

        return new JsonResponse(array_map(fn (Collaboration $c) => $this->serializeFull($c), $list));
    }

    #[Route('/pending', name: 'collaboration_pending', methods: ['GET'])]
    public function pending(CollaborationRepository $repository): JsonResponse
    {
        $list = $repository->findPending();

        return new JsonResponse(array_map(fn (Collaboration $c) => $this->serializeFull($c), $list));
    }

    #[Route('', name: 'collaboration_create', methods: ['POST', 'OPTIONS'])]
    public function create(Request $request, EntityManagerInterface $em): Response
    {
        // Prévol CORS (Authorization + multipart) : sans OPTIONS le routeur renvoie 405.
        if ($request->isMethod('OPTIONS')) {
            return new Response('', Response::HTTP_NO_CONTENT);
        }

        $user = $this->getKeycloakUser();

        $type = strtoupper(trim((string) $request->request->get('type', '')));
        $priority = strtoupper(trim((string) $request->request->get('priority', 'LOW')));
        $publishedAtRaw = (string) $request->request->get('publishedAt', '');

        if (!\in_array($type, ['IMAGE', 'VIDEO'], true)) {
            return new JsonResponse(['error' => 'Type invalide : choisissez IMAGE ou VIDEO dans le formulaire.'], Response::HTTP_BAD_REQUEST);
        }

        if (!\in_array($priority, ['LOW', 'MEDIUM', 'HIGH'], true)) {
            return new JsonResponse(['error' => 'Priorité invalide : LOW, MEDIUM ou HIGH.'], Response::HTTP_BAD_REQUEST);
        }

        /** @var UploadedFile|null $file */
        $file = $request->files->get('file');
        if (!$file instanceof UploadedFile || !$file->isValid()) {
            return new JsonResponse(['error' => 'file is required'], Response::HTTP_BAD_REQUEST);
        }

        $mime = $this->effectiveMimeForUpload($file, $type);
        if (null === $mime) {
            $hint = 'VIDEO' === $type
                ? ' Utilisez une extension .mp4, .webm ou .mov.'
                : ' Utilisez .jpg, .png, .webp ou .gif.';

            return new JsonResponse(['error' => 'Fichier non reconnu pour le type '.$type.'.'.$hint], Response::HTTP_BAD_REQUEST);
        }

        if ($file->getSize() > 50 * 1024 * 1024) {
            return new JsonResponse(['error' => 'File too large (max 50MB)'], Response::HTTP_BAD_REQUEST);
        }

        $ext = strtolower(pathinfo($file->getClientOriginalName(), \PATHINFO_EXTENSION));
        $ext = preg_replace('/[^a-z0-9]/', '', $ext) ?: 'bin';
        $stored = bin2hex(random_bytes(16)).'.'.$ext;

        try {
            $file->move($this->uploadDir(), $stored);
        } catch (FileException $e) {
            return new JsonResponse(['error' => 'Could not store file'], Response::HTTP_INTERNAL_SERVER_ERROR);
        }

        $publishedAt = new \DateTimeImmutable();
        if ('' !== $publishedAtRaw) {
            try {
                $publishedAt = new \DateTimeImmutable($publishedAtRaw);
            } catch (\Exception) {
                return new JsonResponse(['error' => 'Invalid publishedAt'], Response::HTTP_BAD_REQUEST);
            }
        }

        $c = new Collaboration();
        $c->setUserId($user->getSubject());
        $c->setUserName($user->getUsername());
        $c->setType($type);
        $c->setFileName($stored);
        $c->setPublishedAt($publishedAt);
        $c->setPriority($priority);
        $c->setStatus('PENDING');
        $c->setActive(false);

        $em->persist($c);
        $em->flush();

        return new JsonResponse($this->serializeFull($c), Response::HTTP_CREATED);
    }

    #[Route('/{id}/approve', name: 'collaboration_approve', methods: ['PATCH'], requirements: ['id' => '\d+'])]
    public function approve(Collaboration $collaboration, EntityManagerInterface $em): JsonResponse
    {
        if ('PENDING' !== $collaboration->getStatus()) {
            return new JsonResponse(['error' => 'Only PENDING can be approved'], Response::HTTP_BAD_REQUEST);
        }
        $collaboration->setStatus('APPROVED');
        $collaboration->setActive(true);
        $em->flush();

        return new JsonResponse($this->serializeFull($collaboration));
    }

    #[Route('/{id}/reject', name: 'collaboration_reject', methods: ['PATCH'], requirements: ['id' => '\d+'])]
    public function reject(Collaboration $collaboration, EntityManagerInterface $em): JsonResponse
    {
        if ('PENDING' !== $collaboration->getStatus()) {
            return new JsonResponse(['error' => 'Only PENDING can be rejected'], Response::HTTP_BAD_REQUEST);
        }
        $collaboration->setStatus('REJECTED');
        $collaboration->setActive(false);
        $em->flush();

        return new JsonResponse($this->serializeFull($collaboration));
    }

    private function getKeycloakUser(): KeycloakUser
    {
        $u = $this->getUser();
        if (!$u instanceof KeycloakUser) {
            throw new \LogicException('Expected KeycloakUser');
        }

        return $u;
    }

    /**
     * @return array<string, mixed>
     */
    private function serializePublic(Collaboration $c): array
    {
        return [
            'id' => $c->getId(),
            'type' => $c->getType(),
            'priority' => $c->getPriority(),
            'publishedAt' => $c->getPublishedAt()->format(\DateTimeInterface::ATOM),
            'mediaUrl' => '/api/collaborations/media/'.$c->getId(),
        ];
    }

    /**
     * @return array<string, mixed>
     */
    private function serializeFull(Collaboration $c): array
    {
        return [
            'id' => $c->getId(),
            'userId' => $c->getUserId(),
            'userName' => $c->getUserName(),
            'type' => $c->getType(),
            'priority' => $c->getPriority(),
            'publishedAt' => $c->getPublishedAt()->format(\DateTimeInterface::ATOM),
            'status' => $c->getStatus(),
            'active' => $c->isActive(),
            'createdAt' => $c->getCreatedAt()->format(\DateTimeInterface::ATOM),
            'mediaUrl' => '/api/collaborations/media/'.$c->getId(),
        ];
    }
}
