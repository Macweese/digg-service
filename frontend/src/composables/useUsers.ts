import { ref, watch, type Ref } from 'vue';
import { fetchUsers } from '../services/UserService';
import type { User, Page } from '../types';

export function useUsers() {
  const usersPage: Ref<Page<User>> = ref({
    content: [],
    totalPages: 1,
    totalElements: 0,
    number: 0,
  });

  const searchTerm = ref<string>('');
  const itemsPerPage = ref<number>(10);
  const currentPage = ref<number>(0);

  const isLoading = ref<boolean>(false);
  const showLoading = ref<boolean>(false);
  const errorMessage = ref<string>('');
  const toastMessage = ref<string>('');

  const isModalOpen = ref<boolean>(false);
  const editingUser = ref<User | null>(null);

  const isDeleteConfirmOpen = ref<boolean>(false);
  const userToDelete = ref<User['id'] | null>(null);

  let loadingTimeout: number | null = null;

  async function loadUsers(): Promise<void> {
    isLoading.value = true;
    showLoading.value = false;
    if (loadingTimeout) window.clearTimeout(loadingTimeout);
    loadingTimeout = window.setTimeout(() => {
      if (isLoading.value) showLoading.value = true;
    }, 350);

    try {
      const page = await fetchUsers(currentPage.value, itemsPerPage.value, searchTerm.value);
      usersPage.value = page;
      usersPage.value.content = usersPage.value.content || [];
    } catch (e: any) {
      errorMessage.value = 'Could not load users: ' + (e?.message || String(e));
    } finally {
      isLoading.value = false;
      if (loadingTimeout) window.clearTimeout(loadingTimeout);
      window.setTimeout(() => { showLoading.value = false; }, 0);
    }
  }

  // Search / pagination watchers
  watch(searchTerm, () => {
    currentPage.value = 0;
    loadUsers();
  });
  watch(itemsPerPage, () => {
    currentPage.value = 0;
    loadUsers();
  });
  watch(currentPage, () => {
    loadUsers();
  });

  // Modal helpers
  function openAddModal(): void {
    editingUser.value = null;
    isModalOpen.value = true;
  }
  function openEditModal(user: User): void {
    editingUser.value = user;
    isModalOpen.value = true;
  }
  function closeModal(): void {
    isModalOpen.value = false;
    editingUser.value = null;
  }

  // Delete confirm helpers
  function openDeleteConfirm(id: User['id']): void {
    userToDelete.value = id;
    isDeleteConfirmOpen.value = true;
  }
  function closeDeleteConfirm(): void {
    isDeleteConfirmOpen.value = false;
    userToDelete.value = null;
  }

  function nextPage(): void {
    if (currentPage.value < (usersPage.value.totalPages - 1)) currentPage.value++;
  }
  function prevPage(): void {
    if (currentPage.value > 0) currentPage.value--;
  }

  return {
    usersPage,
    searchTerm,
    itemsPerPage,
    currentPage,
    isLoading,
    showLoading,
    errorMessage,
    toastMessage,
    loadUsers,
    nextPage,
    prevPage,
    isModalOpen,
    editingUser,
    openAddModal,
    openEditModal,
    closeModal,
    isDeleteConfirmOpen,
    userToDelete,
    openDeleteConfirm,
    closeDeleteConfirm,
  };
}