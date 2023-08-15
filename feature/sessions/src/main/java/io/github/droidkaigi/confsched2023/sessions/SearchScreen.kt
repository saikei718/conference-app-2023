package io.github.droidkaigi.confsched2023.sessions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.droidkaigi.confsched2023.model.DroidKaigi2023Day
import io.github.droidkaigi.confsched2023.model.Timetable
import io.github.droidkaigi.confsched2023.model.TimetableCategory
import io.github.droidkaigi.confsched2023.model.TimetableItemId
import io.github.droidkaigi.confsched2023.sessions.SearchScreenUiState.Empty
import io.github.droidkaigi.confsched2023.sessions.SearchScreenUiState.SearchList
import io.github.droidkaigi.confsched2023.sessions.component.EmptySearchResultBody
import io.github.droidkaigi.confsched2023.sessions.component.SearchFilter
import io.github.droidkaigi.confsched2023.sessions.component.SearchFilterUiState
import io.github.droidkaigi.confsched2023.sessions.component.SearchTextFieldAppBar
import io.github.droidkaigi.confsched2023.sessions.section.SearchList
import kotlinx.collections.immutable.toPersistentSet

const val searchScreenRoute = "search"
const val SearchScreenTestTag = "SearchScreen"

sealed interface SearchScreenUiState {
    val searchQuery: String
    val searchFilterUiState: SearchFilterUiState

    data class Empty(
        override val searchQuery: String,
        override val searchFilterUiState: SearchFilterUiState,
    ) : SearchScreenUiState

    data class SearchList(
        override val searchQuery: String,
        override val searchFilterUiState: SearchFilterUiState,
        val sessions: Timetable,
    ) : SearchScreenUiState
}

fun NavGraphBuilder.searchScreen(onNavigationIconClick: () -> Unit) {
    composable(searchScreenRoute) {
        SearchScreen(
            onBackClick = onNavigationIconClick,
        )
    }
}

fun NavController.navigateSearchScreen() {
    navigate(searchScreenRoute)
}

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchScreen(
        uiState = uiState,
        modifier = modifier,
        onBackClick = onBackClick,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onDaySelected = viewModel::onDaySelected,
        onFilterCategoryChipClicked = viewModel::onFilterCategoryChipClicked,
        onCategoriesSelected = viewModel::onCategoriesSelected,
    )
}

@Composable
private fun SearchScreen(
    uiState: SearchScreenUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onDaySelected: (DroidKaigi2023Day, Boolean) -> Unit = { _, _ -> },
    onFilterCategoryChipClicked: () -> Unit = {},
    onCategoriesSelected: (TimetableCategory, Boolean) -> Unit = { _, _ -> },
) {
    val scrollState = rememberLazyListState()
    Scaffold(
        modifier = modifier.testTag(SearchScreenTestTag),
        topBar = {
            SearchTextFieldAppBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChanged = onSearchQueryChanged,
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            Divider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline,
            )
            SearchFilter(
                searchFilterUiState = uiState.searchFilterUiState,
                onDaySelected = onDaySelected,
                onFilterCategoryChipClicked = onFilterCategoryChipClicked,
                onCategoriesSelected = onCategoriesSelected,
            )
            when (uiState) {
                is Empty -> EmptySearchResultBody()
                is SearchList -> SearchList(
                    scrollState = scrollState,
                    bookmarkedTimetableItemIds = emptySet<TimetableItemId>().toPersistentSet(),
                    timetableItems = uiState.sessions.timetableItems,
                    onTimetableItemClick = {},
                    onBookmarkIconClick = {},
                )
            }
        }
    }
}
