<?php

namespace App\Repository;

use App\Entity\Collaboration;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Collaboration>
 */
class CollaborationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Collaboration::class);
    }

    /**
     * @return Collaboration[]
     */
    public function findApprovedPublic(\DateTimeInterface $now): array
    {
        return $this->createQueryBuilder('c')
            ->andWhere('c.status = :st')->setParameter('st', 'APPROVED')
            ->andWhere('c.active = :a')->setParameter('a', true)
            ->andWhere('c.publishedAt <= :now')->setParameter('now', $now)
            ->addOrderBy('c.publishedAt', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return Collaboration[]
     */
    public function findPending(): array
    {
        return $this->createQueryBuilder('c')
            ->andWhere('c.status = :st')->setParameter('st', 'PENDING')
            ->orderBy('c.createdAt', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return Collaboration[]
     */
    public function findByUserId(string $userId): array
    {
        return $this->createQueryBuilder('c')
            ->andWhere('c.userId = :u')->setParameter('u', $userId)
            ->orderBy('c.createdAt', 'DESC')
            ->getQuery()
            ->getResult();
    }
}
