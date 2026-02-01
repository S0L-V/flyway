<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div id="toast-container" class="fixed bottom-6 left-1/2 -translate-x-1/2 flex flex-col gap-2 pointer-events-none z-[100]"></div>
<script>
  function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');

    const bgClass = type === 'success' ? 'bg-slate-800 text-white border-slate-700' : 'bg-red-50 text-red-700 border-red-200';
    const iconBgClass = type === 'success' ? 'bg-primary-500' : 'bg-red-500';
    const textClass = type === 'success' ? 'text-white' : 'text-red-800';
    const textColor = type === 'success' ? '#ffffff' : '#7f1d1d';
    const closeBtnClass = type === 'success' ? 'text-white/80' : 'text-red-400';
    const iconName = type === 'success' ? 'check-circle-2' : 'alert-circle';

    toast.className = "toast flex items-center shadow-lg border pointer-events-auto " + bgClass;

    const iconWrap = document.createElement('div');
    iconWrap.className = "p-1 rounded-full " + iconBgClass + " flex items-center justify-center shrink-0";
    const icon = document.createElement('i');
    icon.setAttribute('data-lucide', iconName);
    icon.className = "text-white w-3.5 h-3.5";
    iconWrap.appendChild(icon);

    const text = document.createElement('span');
    text.className = "text-sm font-medium pt-0.5 whitespace-nowrap ml-3 " + textClass;
    text.style.color = textColor;
    text.textContent = message;

    const closeBtn = document.createElement('button');
    closeBtn.type = "button";
    closeBtn.className = "ml-3 hover:opacity-70 flex items-center shrink-0 " + closeBtnClass;
    closeBtn.addEventListener('click', () => toast.remove());
    const closeIcon = document.createElement('i');
    closeIcon.setAttribute('data-lucide', 'x');
    closeIcon.className = "w-4 h-4";
    closeBtn.appendChild(closeIcon);

    toast.appendChild(iconWrap);
    toast.appendChild(text);
    toast.appendChild(closeBtn);

    container.appendChild(toast);
    if (typeof lucide !== "undefined" && typeof lucide.createIcons === "function") {
      lucide.createIcons({ root: toast });
    }

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      setTimeout(() => toast.remove(), 300);
    }, 3000);
  }
</script>
