<?php

namespace App\Security;

use Symfony\Component\Security\Core\User\UserInterface;

class KeycloakUser implements UserInterface
{
    /**
     * @param list<string> $roles Already prefixed with ROLE_
     */
    public function __construct(
        private readonly string $subject,
        private readonly string $username,
        private readonly array $roles,
    ) {
    }

    public function getSubject(): string
    {
        return $this->subject;
    }

    public function getUsername(): string
    {
        return $this->username;
    }

    public function getRoles(): array
    {
        return $this->roles;
    }

    public function eraseCredentials(): void
    {
    }

    public function getUserIdentifier(): string
    {
        return $this->subject;
    }
}
