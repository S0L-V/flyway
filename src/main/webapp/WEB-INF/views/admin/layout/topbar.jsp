<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 fixed top-0 left-64 right-0 z-40">
    <div class="flex items-center w-96 relative">
        <i data-lucide="search" class="absolute left-3 text-slate-400 w-4 h-4"></i>
        <input type="text" placeholder="검색어를 입력하세요..."
               class="w-full bg-slate-50 border-none rounded-md py-2 pl-10 pr-4 text-sm focus:ring-2 focus:ring-blue-500/20 outline-none">
    </div>

    <div class="flex items-center gap-4">
        <button class="relative p-2 text-slate-500 hover:bg-slate-100 rounded-full">
            <i data-lucide="bell" class="w-5 h-5"></i>
            <span class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full border-2 border-white"></span>
        </button>
        <div class="flex items-center gap-3 ml-2 pl-4 border-l border-slate-200">
            <div class="text-right hidden sm:block">
                <p class="text-sm font-semibold text-slate-700">관리자</p>
                <p class="text-[10px] text-slate-400 uppercase font-extrabold tracking-tight">Super Admin</p>
            </div>
            <div class="w-9 h-9 bg-slate-200 rounded-xl overflow-hidden border-2 border-slate-100">
                <img src="https://picsum.photos/100" alt="profile" class="w-full h-full object-cover">
            </div>
        </div>
    </div>
</header>